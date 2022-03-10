package dk.aau.cs.verification.batchProcessing;

public class StatusChangedEvent {
	private final String status;
	
	public StatusChangedEvent(String status) {
		this.status = status;
	}
	
	public String status() {
		return status;
	}
}
