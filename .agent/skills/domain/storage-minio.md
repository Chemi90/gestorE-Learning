# Skill: Almacenamiento de Objetos (MinIO / S3)

## Propósito
Convenciones obligatorias para la integración con MinIO (u otros proveedores S3 compatibles) para el almacenamiento de archivos binarios, documentos e imágenes en los microservicios.

## Reglas Obligatorias

1. **Dependencia Única**: Utilizar el SDK oficial `io.minio:minio`.
2. **Nomenclatura de Archivos (Object Names)**:
   - NUNCA guardar el archivo en MinIO con su nombre original enviado por el usuario.
   - SIEMPRE generar un `UUID` para el archivo en MinIO.
   - Formato requerido: `<UUID>.<extensión>` (ej. `123e4567-e89b-12d3-a456-426614174000.pdf`).
   - El nombre original (`filename`) y los metadatos deben persistirse siempre en una tabla de PostgreSQL.
3. **Estructura de Buckets**:
   - Se utiliza la convención de un bucket por servicio que lo requiera o un dominio principal. Para el entorno de e-learning, usar prefijos del servicio si comparten servidor (ej. `elearning-rag`, `elearning-content`).
   - El nombre del bucket debe ser inyectado vía propiedades (`application.yml`), nunca hardcodeado.
4. **Inicialización del Bucket**:
   - El servicio responsable (ej. `MinioService`) DEBE verificar la existencia del bucket al arrancar (ej. usando un método anotado con `@PostConstruct` o en el constructor) y crearlo automáticamente si no existe.
5. **Manejo de Errores y Excepciones**:
   - Nunca propagar excepciones nativas de MinIO (`MinioException`, `ServerException`, etc.) hacia el controlador REST.
   - Capturar las excepciones en la capa de servicio y envolverlas en `ResponseStatusException` (ej. `500 Internal Server Error` o `502 Bad Gateway`) con un mensaje claro, para que el `GlobalExceptionHandler` genere el "Error envelope" correcto de la API.

## Plantilla Base (MinioService)

```java
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioService(
            MinioClient minioClient,
            @Value("${minio.bucket.name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @PostConstruct
    public void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando bucket de MinIO: " + bucketName, e);
        }
    }

    public String uploadFile(MultipartFile file, String extension) {
        String objectName = UUID.randomUUID().toString() + extension;
        
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error al subir el archivo al almacenamiento"
            );
        }
    }
}
```

## Propiedades (application.yml)

Los servicios que usen MinIO deben requerir las siguientes propiedades:

```yaml
minio:
  url: ${MINIO_URL:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin123}
  bucket:
    name: ${MINIO_BUCKET_NAME:<nombre-servicio>}
```
