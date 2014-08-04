/*
 * Copyright (C) 2014 Saurabh Rane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geoservicesapi;

import com.geoservicesapi.YelpApi;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class Yelp {

	OAuthService service;
	Token accessToken;
	
	/**
	   * Setup the Yelp API OAuth credentials.
	   * 
	   * @param consumerKey Consumer key
	   * @param consumerSecret Consumer secret
	   * @param token Token
	   * @param tokenSecret Token secret
	   */
	private Yelp(String consumerKey, String consumerSecret, String token, String tokenSecret) {
		this.service = new ServiceBuilder().provider(YelpApi.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
		this.accessToken = new Token(token, tokenSecret);
	}

	/**
	   * Creates and sends a request to the Search API by term and location.
	   * <p>
	   * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
	   * for more info.
	   * 
	   * @param term <tt>String</tt> of the search term to be queried
	   * @param latitude <tt>double</tt> of the location
	   * @param longitude <tt>double</tt> of the location
	   * @return <tt>String</tt> JSON Response
	   */
	private String search(String term, double latitude, double longitude) {
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
		request.addQuerystringParameter("term", term);
		request.addQuerystringParameter("ll", latitude + "," + longitude);		
		request.addQuerystringParameter("limit", "20");
		this.service.signRequest(this.accessToken, request);
		Response response = request.send();
		return response.getBody();
	}
	
	/**
	   * Creates and sends a request to the Search API by term and location.
	   * 
	   * @param consumerKey Consumer key
	   * @param consumerSecret Consumer secret
	   * @param token Token
	   * @param tokenSecret Token secret
	   * @param latitude <tt>double</tt> of the location
	   * @param longitude <tt>double</tt> of the location
	   * @return <tt>String</tt> JSON Response
	   */
	public static String getResponse(String consumerKey, String consumerSecret, String token, String tokenSecret, double latitude, double longitude) {
		Yelp yelp = new Yelp(consumerKey, consumerSecret, token, tokenSecret);
		
		//Change "restaurants" to anything you want to search
		String response = yelp.search("restaurants", latitude, longitude);
		
		return response;
	}
}
