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
package eu.trentorise.smartcampus.trentinofamiglia.update;


public enum Status {

	OK(0), NOT_FOUND(1), NOT_VALID_UID(2), NOT_VALID_SIGNATURE(3), NOT_UPDATED(3);

	private int mStatus;

	private Status(int status) {
		mStatus = status;
	}

	public int value() {
		return mStatus;
	}
}
