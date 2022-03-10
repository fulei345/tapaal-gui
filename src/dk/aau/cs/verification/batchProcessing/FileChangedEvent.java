package dk.aau.cs.verification.batchProcessing;

public class FileChangedEvent {
	private final String fileName;
	
	public FileChangedEvent(String fileName) {
		this.fileName = fileName;
	}
	
	public String fileName() {
		return fileName;
	}
}
