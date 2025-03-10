package nl.nn.adapterframework.filesystem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hamcrest.core.StringEndsWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.nn.adapterframework.stream.Message;
import nl.nn.adapterframework.util.FilenameUtils;
import nl.nn.adapterframework.util.StreamUtil;

public abstract class BasicFileSystemTest<F, FS extends IBasicFileSystem<F>> extends FileSystemTestBase {

	protected FS fileSystem;
	/**
	 * Returns the file system
	 */
	protected abstract FS createFileSystem();

	@BeforeEach
	@Override
	public void setUp() throws Exception {
		fileSystem = createFileSystem();
	}

	@AfterEach
	@Override
	public void tearDown() throws Exception {
		if (fileSystem!=null) fileSystem.close();
	}

	@Test
	public void basicFileSystemTestConfigure() throws Exception {
		fileSystem.configure();
	}

	@Test
	public void basicFileSystemTestOpen() throws Exception {
		fileSystem.configure();
		fileSystem.open();
	}

	@Test
	public void basicFileSystemTestExists() throws Exception {
		String filename = "testExists" + FILE1;

		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, "tja");
		waitForActionToFinish();

		// test
		F f = fileSystem.toFile(filename);
		assertTrue(fileSystem.exists(f), "Expected file[" + filename + "] to be present");
	}

	@Test
	public void basicFileSystemTestNotExists() throws Exception {
		String filename = "testNotExists" + FILE1;

		fileSystem.configure();
		fileSystem.open();

		deleteFile(null, filename);
		waitForActionToFinish();

		// test
		F f = fileSystem.toFile(filename);
		assertFalse(fileSystem.exists(f), "Expected file[" + filename + "] not to be present");
	}


	@Override
	protected void equalsCheck(String content, String actual) {
		assertEquals(content, actual);
	}


	@Override
	protected void existsCheck(String filename) throws Exception {
		assertTrue(_fileExists(filename), "Expected file [" + filename + "] to be present");
	}



	@Test
	public void basicFileSystemTestDelete() throws Exception {
		String filename = "tobeDeleted" + FILE1;

		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, "maakt niet uit");
		waitForActionToFinish();
		existsCheck(filename);

		// test
		F file = fileSystem.toFile(filename);
		fileSystem.deleteFile(file);
		waitForActionToFinish();

		assertFalse(_fileExists(filename), "Expected file [" + filename + "] not to be present");
	}

	public void testReadFile(F file, String expectedContents, String charset) throws IOException, FileSystemException {
		Message in = fileSystem.readFile(file, charset);
		String actual = in.asString();
		// test
		equalsCheck(expectedContents.trim(), actual.trim());
	}

	@Test
	public void basicFileSystemTestRead() throws Exception {
		String filename = "read" + FILE1;
		String contents = "Tekst om te lezen";

		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, contents);
		waitForActionToFinish();
		// test
		existsCheck(filename);

		F file = fileSystem.toFile(filename);
		// test
		testReadFile(file, contents, null);
	}

	@Test
	public void basicFileSystemTestReadAndPreserve() throws Exception {
		String filename = "read" + FILE1;
		String contents = "Tekst om te lezen";

		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, contents);
		waitForActionToFinish();
		// test
		existsCheck(filename);

		F file = fileSystem.toFile(filename);
		// test
		Message in = fileSystem.readFile(file, null);

		// preserve() converts non repeatable messages to byte array
		in.preserve();

		// test if message can actually be read multiple times, without turning it explicitly into a String or byte array.
		// This will fail if a message declared that it was repeatable, but actually was not repeatable.
		String actual1 = StreamUtil.readerToString(in.asReader(), null);
		equalsCheck(contents, actual1.trim());
		String actual2 = StreamUtil.readerToString(in.asReader(), null);
		equalsCheck(contents, actual2.trim());
	}

	@Test
	public void basicFileSystemTestReadSpecialChars() throws Exception {
		String filename = "readSpecial" + FILE1;
		String contents = "€ $ & ^ % @ < é ë ó ú à è";

		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, contents);
		waitForActionToFinish();
		// test
		existsCheck(filename);

		F file = fileSystem.toFile(filename);
		// test
		testReadFile(file, contents, "UTF-8");
	}

	@Test
	public void basicFileSystemTestReadSpecialCharsFails() throws Exception {
		String filename = "readSpecialChars" + FILE1;
		String contents = "€ é";
		String expected = "â¬ Ã©";
		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, contents);
		waitForActionToFinish();
		// test
		existsCheck(filename);

		F file = fileSystem.toFile(filename);
		// test
		testReadFile(file, expected, "ISO-8859-1");
	}

	@Test
	public void basicFileSystemTestGetName() throws Exception {
		String filename = "readName" + FILE1;
		String contents = "Tekst om te lezen";

		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, contents);
		waitForActionToFinish();

		F file = fileSystem.toFile(filename);
		// test
		assertEquals(filename, fileSystem.getName(file));
	}

	@Test
	public void basicFileSystemTestModificationTime() throws Exception {
		String filename = "readModificationTime" + FILE1;
		String contents = "Tekst om te lezen";
		Date date = new Date();

		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, contents);
		waitForActionToFinish();

		F file = fileSystem.toFile(filename);
		Date actual = fileSystem.getModificationTime(file);
		long diff = actual.getTime() - date.getTime();

		fileSystem.deleteFile(file);
		waitForActionToFinish();
		// test
		assertFalse(diff > 10000);
	}


	@Test
	public void basicFileSystemTestMoveFile() throws Exception {
		String filename = "fileTobeMoved.txt";
		String contents = "tja";
		String srcFolder = "srcFolder";
		String dstFolder = "dstFolder";

		fileSystem.configure();
		fileSystem.open();

		_createFolder(srcFolder);
		createFile(srcFolder,filename, contents);
		waitForActionToFinish();

		assertFileExistsWithContents(srcFolder, filename, contents);

		_createFolder(dstFolder);
		waitForActionToFinish();

		assertTrue(_folderExists(dstFolder));
		assertFileDoesNotExist(dstFolder, filename);

		F f = fileSystem.toFile(srcFolder, filename);
		F f2 = fileSystem.toFile(srcFolder, filename);
		F movedFile =fileSystem.moveFile(f, dstFolder, false, true);
		waitForActionToFinish();

		assertEquals(filename,fileSystem.getName(movedFile));

		assertTrue(_folderExists(dstFolder), "Destination folder must exist");
		assertFileExistsWithContents(dstFolder, fileSystem.getName(movedFile), contents);
		//TODO: test that contents of file has remained the same
		//TODO: test that file timestamp has not changed
		assertFileDoesNotExist(srcFolder, filename);

		assertFalse(fileSystem.exists(f2), "original file should not exist anymore after move");

		try {
			F movedFile2 =fileSystem.moveFile(f2, dstFolder, false, true);
			assertNull(movedFile2, "File should not be moveable again");
		} catch (Exception e) {
			// an exception will do too, to signal that the file cannot be moved again.
			log.debug("exception caught after trying to move file: "+e.getMessage());
		}
	}

	@Test
	public void basicFileSystemTestMoveFileMustFailWhenTargetAlreadyExists() throws Exception {
		String filename = "fileTobeMoved.txt";
		String srcContents = "fakeSourceContents";
		String dstContents = "fakeDestinationContents";
		String srcFolder = "srcFolder";
		String dstFolder = "dstFolder";

		fileSystem.configure();
		fileSystem.open();

		_createFolder(srcFolder);
		createFile(srcFolder,filename, srcContents);
		_createFolder(dstFolder);
		createFile(dstFolder,filename, dstContents);
		waitForActionToFinish();

		assertFileExistsWithContents(srcFolder, filename, srcContents);
		assertFileExistsWithContents(dstFolder, filename, dstContents);

		F f = fileSystem.toFile(srcFolder, filename);

		assertThrows(FileSystemException.class, ()-> fileSystem.moveFile(f, dstFolder, false, true) );
	}

	@Test
	public void basicFileSystemTestCopyFile() throws Exception {
		String filename = "fileTobeCopied.txt";
		String contents = "tja";
		String srcFolder = "srcFolder";
		String dstFolder = "dstFolder";

		fileSystem.configure();
		fileSystem.open();

		_createFolder(srcFolder);
		createFile(srcFolder,filename, contents);
		waitForActionToFinish();

		assertFileExistsWithContents(srcFolder, filename, contents);

		_createFolder(dstFolder);
		waitForActionToFinish();

		assertTrue(_folderExists(dstFolder));
		assertFileDoesNotExist(dstFolder, filename);

		F f = fileSystem.toFile(srcFolder, filename);
		F copiedFile = fileSystem.copyFile(f, dstFolder, false, true);
		waitForActionToFinish();

		assertEquals(filename, fileSystem.getName(copiedFile));

		assertTrue(_folderExists(dstFolder), "Destination folder must exist");
		assertFileExistsWithContents(dstFolder, fileSystem.getName(copiedFile), contents);
		//TODO: test that contents of file has remained the same
		//TODO: test that file timestamp has not changed
		assertFileExistsWithContents(srcFolder, filename, contents);
	}




	@Test
	public void basicFileSystemTestExistsMethod() throws Exception {
		String fileName = "fileExists.txt";

		fileSystem.configure();
		fileSystem.open();

		createFile(null, fileName, "");
		waitForActionToFinish();
		F f = fileSystem.toFile(fileName);

		assertTrue(fileSystem.exists(f));
	}

	public void basicFileSystemTestListFile(String folder, int numOfFilesInFolder) throws Exception {
		String contents = "maakt niet uit ";

		fileSystem.configure();
		fileSystem.open();

		long beforeFilesCreated=System.currentTimeMillis();

		for (int i=0; i<numOfFilesInFolder; i++) {
			createFile(folder, "file_"+i+".txt", contents+i);
		}
		waitForActionToFinish();

		long afterFilesCreated=System.currentTimeMillis();

		Set<F> files = new HashSet<F>();
		Set<String> filenames = new HashSet<String>();
		int count = 0;
		try(DirectoryStream<F> ds = fileSystem.listFiles(folder)) {
			Iterator<F> it = ds.iterator();
			// Count files
			while (it.hasNext()) {
				F f=it.next();
				files.add(f);
				String name=fileSystem.getName(f);
				log.debug("found file ["+name+"]");
				filenames.add(name);
				count++;
			}
		}

		assertEquals(numOfFilesInFolder, files.size(), "Size of set of files");
		assertEquals(numOfFilesInFolder, filenames.size(), "Size of set of filenames");

		if (folder==null) {
			for (String filename:filenames) {
				F f=fileSystem.toFile(filename);
				assertNotNull(f, "file must be found by filename ["+filename+"]");
				assertTrue(fileSystem.exists(f), "file must exist when referred to by filename ["+filename+"]");
			}
		}
		try(DirectoryStream<F> ds = fileSystem.listFiles(folder)) {
			Iterator<F> it = ds.iterator();
			for (int i = 0; i < count; i++) {
				assertTrue(it.hasNext());
				it.next();
			}
			// test
			assertFalse(it.hasNext());
		}

		if (numOfFilesInFolder>0) {
			deleteFile(folder, "file_0.txt");
			int numDeleted = 1;

			waitForActionToFinish();

			assertFalse(_fileExists(folder, "file_0.txt"), "file should not exist anymore physically after deletion");

			try(DirectoryStream<F> ds = fileSystem.listFiles(folder)) {
				Iterator<F> it = ds.iterator();
				for (int i = 0; i < count - numDeleted; i++) {
					assertTrue(it.hasNext());
					F f=it.next();
					log.debug("found file ["+fileSystem.getName(f)+"]");
					assertTrue(_fileExists(folder, fileSystem.getName(f)), "file found should exist");
					long modTime=fileSystem.getModificationTime(f).getTime();
					if (doTimingTests) assertTrue(modTime>=beforeFilesCreated, "modtime ["+modTime+"] not after t0 ["+beforeFilesCreated+"]");
					if (doTimingTests) assertTrue(modTime<=afterFilesCreated, "modtime ["+modTime+"] not before t1 ["+afterFilesCreated+"]");
				}
				// test
				assertFalse(it.hasNext(), "after a delete the number of files should be one less");
			}

			if (numOfFilesInFolder>1) {
				deleteFile(folder, "file_1.txt");
				numDeleted++;

				try(DirectoryStream<F> ds = fileSystem.listFiles(folder)) {
					Iterator<F> it = ds.iterator();
					for (int i = 0; i < count - numDeleted; i++) {
						assertTrue(it.hasNext());
						it.next();
					}
					// test
					assertFalse(it.hasNext());
				}
			}
		}
	}

	@Test
	public void basicFileSystemTestListFileFromRoot() throws Exception {
		_deleteFolder(null); //Clean root folder
		basicFileSystemTestListFile(null, 2);
	}
	@Test
	public void basicFileSystemTestListFileFromFolder() throws Exception {
		_deleteFolder(null); //Clean root folder
		_createFolder("folder");
		basicFileSystemTestListFile("folder", 2);
	}

	@Test
	public void basicFileSystemTestListFileFromEmptyFolder() throws Exception {
		_deleteFolder(null); //Clean root folder
		_createFolder("folder2");
		basicFileSystemTestListFile("folder2", 0);
	}

	@Test
	public void basicFileSystemTestListFileShouldNotReadFromOtherFoldersWhenReadingFromRoot() throws Exception {
		_deleteFolder(null); //Clean root folder
		_createFolder("folder");
		_createFolder("Otherfolder");
		createFile("Otherfolder", "otherfile", "maakt niet uit");
		basicFileSystemTestListFile(null, 2);
	}

	@Test
	public void basicFileSystemTestListFileShouldNotReadFromOtherFoldersWhenReadingFromFolder() throws Exception {
		_deleteFolder(null); //Clean root folder
		_createFolder("folder");
		_createFolder("Otherfolder");
		createFile("Otherfolder", "otherfile", "maakt niet uit");
		basicFileSystemTestListFile("folder", 2);
	}

	@Test
	public void basicFileSystemTestListFileShouldNotReadFromRootWhenReadingFromFolder() throws Exception {
		_deleteFolder(null); //Clean root folder
		_createFolder("folder");
		createFile(null, "otherfile", "maakt niet uit");
		basicFileSystemTestListFile("folder", 2);
	}

	@Test
	public void basicFileSystemTestListFileShouldNotReadFolders() throws Exception {
		_deleteFolder(null);
		String contents1 = "maakt niet uit";
		String contents2 = "maakt ook niet uit";
		String folderName = "subfolder";

		fileSystem.configure();
		fileSystem.open();


		createFile(null, FILE1, contents1);
		createFile(null, FILE2, contents2);
		_createFolder(folderName);

		Set<F> files = new HashSet<F>();
		Set<String> filenames = new HashSet<String>();
		try(DirectoryStream<F> ds = fileSystem.listFiles(null)) {
			Iterator<F> it = ds.iterator();
			// Count files
			while (it.hasNext()) {
				F f=it.next();
				files.add(f);
				filenames.add(fileSystem.getName(f));
			}
		}

		assertEquals(2, files.size(), "Size of set of files, should not contain folders");
		assertEquals( 2, filenames.size(), "Size of set of filenames, should not contain folders");

	}

	@Test
	public void basicFileSytemTestGetNumberOfFilesInFolder() throws Exception {
		// arrange
		String contents1 = "maakt niet uit";
		String contents2 = "maakt ook niet uit";
		String folderName = "folder_for_counting";

		if (_folderExists(folderName)) {
			_deleteFolder(folderName);
		}
		_createFolder(folderName);

		fileSystem.configure();
		fileSystem.open();


		// act
		int fileCount = fileSystem.getNumberOfFilesInFolder(folderName);

		// assert
		assertEquals(0, fileCount);


		// arrange 2
		createFile(folderName, FILE1, contents1);
		createFile(folderName, FILE2, contents2);

		// act 2
		fileCount = fileSystem.getNumberOfFilesInFolder(folderName);

		// assert 2
		assertEquals(2, fileCount);
	}

	@Test
	// getParentFolder() is used when attribute deleteEmptyFolder=true, and in action RENAME
	public void basicFileSystemTestGetParentOfTheDeletedFile() throws Exception {
		String folderName = "parentFolder";

		fileSystem.configure();
		fileSystem.open();

		_createFolder(folderName);
		createFile(folderName, FILE1, "text");

		F f = fileSystem.toFile(folderName, FILE1);

		fileSystem.deleteFile(f);

		String parentFolder = fileSystem.getParentFolder(f);
		parentFolder = FilenameUtils.normalizeNoEndSeparator(parentFolder);

		assertThat(parentFolder, StringEndsWith.endsWith(folderName));
	}

}