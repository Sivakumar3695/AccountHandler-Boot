package com.application.acchandler.storage;

import com.application.acchandler.model.users.Users;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

	void init();

	String store(Users user, MultipartFile file, String fileName);

	void delete(String fileName) throws Exception;
//
//	Stream<Path> loadAll();
//
	Path load(String filename);
//
	byte[] loadAsResource(Users user, String filename) throws Exception;
//
//	void deleteAll();

}
