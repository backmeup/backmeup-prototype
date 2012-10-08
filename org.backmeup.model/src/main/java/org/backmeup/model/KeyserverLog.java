package org.backmeup.model;

public class KeyserverLog
{
	private String message;
	private Long date;

	public String getMessage ()
	{
		return message;
	}
	
	public void setMessage (String message)
	{
		this.message = message;
	}

	public Long getDate ()
	{
		return date;
	}
	
	public void setDate (Long date)
	{
		this.date = date;
	}
}
