package gov.epa.festc.gui;

public interface MessageBoard {
	void clear();
    void setError(String error);
    void setMessage(String message);
}
