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

import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Review implements Serializable {
	private static final long serialVersionUID = -6620600757273551263L;
	
	private String userId;
	private String comment;
	private Integer rating;
	private String author;
	private long date;
	
	private static final Format formatter = new SimpleDateFormat("dd/MM/yyyy");

	public Review() {
		super();
	}
	public Review(String user, String comment, int rating) {
		this.userId = user;
		this.comment = comment;
		this.rating = rating;
		this.date = System.currentTimeMillis();
				
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}

	public String formattedDate() {
		return formatter.format(new Date(date));
	}
	
}
