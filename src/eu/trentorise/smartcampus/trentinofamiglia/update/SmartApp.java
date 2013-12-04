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

import android.graphics.drawable.Drawable;


public class SmartApp {

	public String appPackage;
	public String name;
	public String url;
	public Drawable icon;
	public Drawable gray_icon;
	public String background;
	public int version;
	public String filename;

	public void fillApp(String name, String pack, String url, Drawable icon, Drawable gray, String background, int versions, String filename) {
		this.name = name;
		this.appPackage = pack;
		this.url = url;
		this.icon = icon;
		this.gray_icon = gray;
		this.background = background;
		this.version = versions;
		this.filename = filename;
	}

}
