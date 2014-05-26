package de.imc.advancedMediaSearch.result;

import java.util.Date;

public class ResultComment implements Cloneable{

    private String userId;
    private String userEmail;
    private Date date;
    private String text;
    private String source;

    /**
     * @param author
     * @param email
     * @param date
     * @param text
     */
    public ResultComment(String author, String email, Date date, String text, String source) {
        super();
        this.userId = author;
        this.userEmail = email;
        this.date = date;
        this.text = text;
        this.source = source;
    }

    /**
     * 
     */
    public ResultComment() {
        super();
        this.userId = "";
        this.userEmail = "";
        this.date = new Date();
        this.text = "";
        this.source = "";
    }

    /**
     * @return the author
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the user id to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the email
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * @param email the email to set
     */
    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

	/**
	 * @param source the source repository id to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the source repositories id
	 */
	public String getSource() {
		return source;
	}
	
    public Object clone(){
        ResultComment n = new ResultComment();
        n.setUserId(this.getUserId());
        n.setDate(this.getDate());
        n.setUserEmail(this.getUserEmail());
        n.setText(this.getText());
        n.setSource(this.getSource());
        return n;
    }

}
