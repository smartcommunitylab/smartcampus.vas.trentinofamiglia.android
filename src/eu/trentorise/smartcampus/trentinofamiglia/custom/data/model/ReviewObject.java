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

import java.util.ArrayList;
import java.util.List;

public class ReviewObject  {

	protected String id;
	
	protected List<Review> reviews;

	public ReviewObject() {
	}
	
	public ReviewObject(String id) {
		this.id = id;
		reviews = new ArrayList<Review>();
	}
	
	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	public String getParentId() {
		return id;
	}

	public void setParentId(String parentId) {
		this.id = parentId;
	}
	
	
	
}
