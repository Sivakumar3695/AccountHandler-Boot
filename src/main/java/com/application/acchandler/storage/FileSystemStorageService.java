package com.application.acchandler.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.application.acchandler.exceptions.CustomException;
import com.application.acchandler.model.users.Users;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
public class FileSystemStorageService implements StorageService {

	private final WebClient webClient;

	@Value("${app.source}")
	private String appSource;

	private static final Path tempFileLocation = Paths.get("/tmp");
	private static final Logger logger = LoggerFactory.getLogger("FileSystemStorageService");


	@Autowired
	public FileSystemStorageService(StorageProperties properties) {

		HttpClient httpClient = HttpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
				.responseTimeout(Duration.ofMillis(10000))
				.doOnConnected(conn ->
						conn
								.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS))
								.addHandlerLast(new WriteTimeoutHandler(10000, TimeUnit.MILLISECONDS)));

		this.webClient = WebClient.builder()
				.baseUrl(properties.getLocation())
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
				.defaultUriVariables(Collections.singletonMap("url", properties.getLocation()))
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}

	private Path createTempCopyFile(MultipartFile file, String fileName) throws Exception
	{
		if (file.isEmpty()) {
			throw new StorageException("Failed to store empty file.");
		}
		Path destinationFile = tempFileLocation.resolve(
						Paths.get(fileName))
				.normalize().toAbsolutePath();
		if (!destinationFile.getParent().equals(tempFileLocation.toAbsolutePath())) {
			// This is a security check
			throw new StorageException(
					"Cannot store file outside current directory.");
		}
		try (InputStream inputStream = file.getInputStream()) {
			Files.copy(inputStream, destinationFile,
					StandardCopyOption.REPLACE_EXISTING);
		}

		return destinationFile;
	}

	@Override
	public String store(Users user, MultipartFile file, String fileName) {

		Path destinationFile = null;
		try {
			destinationFile = createTempCopyFile(file, fileName);

			// external API call to Storage Service using WebClient begins.

			MultipartBodyBuilder builder = new MultipartBodyBuilder();
			builder.part("file", new FileSystemResource(destinationFile));
			builder.part("app-source", appSource);

			Mono<StorageSvcResponse> responseMono = this.webClient.post()
					.uri("/api/users/"+ user.getUserID() + "/upload")
					.contentType(MediaType.valueOf(file.getContentType()))
					.body(BodyInserters.fromMultipartData(builder.build()))
					.exchangeToMono(response -> {
						if (response.statusCode().equals(HttpStatus.OK))
						{
							logger.info("Successfully uploaded file in Storage Service.");
							return response.bodyToMono(StorageSvcResponse.class);
						}
						logger.info("File upload to storage service unsuccessful");
						return Mono.just(new StorageSvcResponse(null, null, null));
					});

			StorageSvcResponse storageSvcResponse = responseMono.block();

			// external API call to Storage Service using WebClient ends.

			if (storageSvcResponse.getFileName() == null)
				throw new CustomException("There is an error in processing the request");

			logger.info(storageSvcResponse.getFileName());

			return storageSvcResponse.getFileName();
		}
		catch (Exception e) {
			throw new StorageException("Failed to store file.", e);
		}
		finally {
			try {
				if (destinationFile != null) delete(destinationFile.getFileName().toString());
			}
			catch (Exception e) {
				logger.info("Exception occurred while removing files from /tmp folder.");
			}
		}
	}

//	@Override
//	public Stream<Path> loadAll() {
//		try {
//			return Files.walk(this.rootLocation, 1)
//				.filter(path -> !path.equals(this.rootLocation))
//				.map(this.rootLocation::relativize);
//		}
//		catch (IOException e) {
//			throw new StorageException("Failed to read stored files", e);
//		}
//
//	}
//
	@Override
	public Path load(String filename) {
		return tempFileLocation.resolve(filename);
	}

	@Override
	public byte[] loadAsResource(Users user, String filename) throws Exception {
		Mono<byte[]> responseMono = this.webClient.get()
				.uri("/api/users/"+ user.getUserID() + "/file?file-name="+filename+"&app-source="+appSource)
				.accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)
				.exchangeToMono(response -> {
					if (response.statusCode().equals(HttpStatus.OK))
					{
						logger.info("Successfully uploaded file in Storage Service.");
						return response.bodyToMono(byte[].class);
					}
					logger.info("File loading from storage service unsuccessful");
					return Mono.just(new byte[]{});
				});
		byte[] response = responseMono.block();
		if (response.length == 0) throw new Exception("There is an error in processing the request");

		return response;
	}

	@Override
	public void delete(String fileName) throws Exception {
		Path file = load(fileName);
		FileSystemUtils.deleteRecursively(file);
	}

	@Override
	public void init() {
		try {
			if (!Files.isDirectory(tempFileLocation))
				Files.createDirectories(tempFileLocation);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}
}
