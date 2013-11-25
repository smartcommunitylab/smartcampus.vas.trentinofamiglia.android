/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.trentinofamiglia.custom.data.model;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TmpComment {
	private String text, author;
	private Date date;
	private Format formatter;

	public TmpComment(String text, String author, Date date) {
		super();
		this.text = text;
		this.author = author;
		this.date = date;
		this.formatter = new SimpleDateFormat("d/MMM/yy");
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDate() {
		return formatter.format(date);
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
