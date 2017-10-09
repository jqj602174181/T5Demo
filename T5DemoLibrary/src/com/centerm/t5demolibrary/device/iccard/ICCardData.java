package com.centerm.t5demolibrary.device.iccard;

public class ICCardData
{
	public String timeOut = "20";
	public String ARQC = "";
	public String list = "";
	public String tag = "A";
	public int cardStyle = 1;

	public int getCardStyle()
	{
		return cardStyle;
	}

	public void setCardStyle(int cardStyle)
	{
		this.cardStyle = cardStyle;
	}
}
