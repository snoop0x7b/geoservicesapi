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

package com.geoservicesapi.services;

import org.json.JSONObject;
import org.json.JSONArray;

import com.geoservicesapi.Yelp;
import com.geoservicesapi.services.LocationServices;

/**
 * PlacesServices is a class for consuming places services using Yelp Places API and MapQuest Geo-coding API.
 * 
 * @author Saurabh Rane
 * @version 2014-07-24
*/

public class PlacesServices {
	
	private String consumerKey;
	private String consumerSecret;
	private String token;
	private String tokenSecret;
	private String mapquestKey;
	
    /**
     * Construct a PlacesServices with your yelp api key and mapquest api key.
     *
     * @param consumerKey
     *            Your consumer key for yelp api.
     * @param consumerSecret
     *            Your consumer secret for yelp api.
     * @param token
     *            Your token for yelp api.
     * @param tokenSecret
     *            Your token secret for yelp api.
     * @param mapquestKey
     *            Your key for mapquest api.
     */
	public PlacesServices(String consumerKey, String consumerSecret, String token, String tokenSecret, String mapquestKey) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.token = token;
		this.tokenSecret = tokenSecret;
		this.mapquestKey = mapquestKey;
	}
	
    /**
     * Get the places around a location using its coordinates.
     *
     * @param lat
     *            Latitude of the location.
     * @param lng
     *            Longitude of the location.
     * @return The JSONObject associated with information about places.
     */
	public JSONObject getVenues(String lat, String lng) {
		
		JSONObject result = new JSONObject();
		JSONArray venues = new JSONArray();
		if(lat == null) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are missing in request.");
			error.put("id", "MISSING_PARAMETER");
			error.put("field", "lat");
			result.put("error", error);
		} else if(lng == null) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are missing in request.");
			error.put("id", "MISSING_PARAMETER");
			error.put("field", "lng");
			result.put("error", error);
		} else if(lat.equals("")) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are invalid in request.");
			error.put("id", "INVALID_PARAMETER");
			error.put("field", "lat");
			result.put("error", error);
		} else if(lng.equals("")) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are invalid in request.");
			error.put("id", "INVALID_PARAMETER");
			error.put("field", "lng");
			result.put("error", error);
		} else {
			boolean latError = false, lngError = false;
			double latitude = 37.774929, longitude = -122.419416;
			
			try {
				latitude = Double.parseDouble(lat);
			} catch(Exception e) {
				latError = true;
			}
			
			try {
				longitude = Double.parseDouble(lng);
			} catch(Exception e) {
				lngError = true;
			}
			
			if(latError) {
				JSONObject error = new JSONObject();
				error.put("message", "One or more parameters in request are not in required format.");
				error.put("id", "INVALID_FORMAT");
				error.put("field", "lat");
				result.put("error", error);
			} else if(lngError) {
				JSONObject error = new JSONObject();
				error.put("message", "One or more parameters in request are not in required format.");
				error.put("id", "INVALID_FORMAT");
				error.put("field", "lng");
				result.put("error", error);
			} else {
				String response = Yelp.getResponse(consumerKey, consumerSecret, token, tokenSecret, latitude, longitude);
				JSONObject responseJson = new JSONObject(response);
				JSONArray businesses = null;
				try {
					businesses = responseJson.getJSONArray("businesses");
				} catch(Exception e) {
				}
				if(businesses == null) {
					JSONObject error = responseJson.getJSONObject("error");
					String id = error.getString("id");
					if(id.equalsIgnoreCase("UNAVAILABLE_FOR_LOCATION")) {
						result.put("error", error);
					}
				} else {
					LocationServices lr = new LocationServices(mapquestKey);

					for(int index=0; index<businesses.length(); index++) {
						JSONObject business = businesses.getJSONObject(index);
						String name = business.getString("name");
						String displayPhone = "";
						double distance = -1;
						try {
							distance = business.getDouble("distance");
						} catch(Exception ignore) {
						}

						try {
							displayPhone = business.getString("display_phone");
						} catch(Exception ignore) {
						}
						
						JSONObject location = business.getJSONObject("location");
						JSONArray address = location.getJSONArray("address");
						String addressString = "", city = "", state = "", postalCode = "", country = "";
						
						try {
							addressString = address.getString(0);
						} catch(Exception ignore) {
						}

						try {
							city = location.getString("city");
						} catch(Exception ignore) {
						}

						try {
							state = location.getString("state_code");
						} catch(Exception ignore) {
						}

						try {
							country = location.getString("country_code");
						} catch(Exception ignore) {
						}

						try {
							postalCode = location.getString("postal_code");
						} catch(Exception ignore) {
						}

						JSONObject venue = new JSONObject();
						venue.put("name", name);
						venue.put("address", addressString);
						venue.put("city", city);
						venue.put("state", state);
						venue.put("postalCode", postalCode);
						venue.put("country", country);
						venue.put("distance", distance);
						venue.put("formattedPhone", displayPhone);
						
						JSONObject latLng = lr.getCoordinatesUsingComponents(addressString, city, state, postalCode);
						JSONObject loc = latLng.getJSONObject("location");
						double mqrLat = loc.getDouble("lat");
						double mqrLng = loc.getDouble("lng");
						
						venue.put("lat", mqrLat);
						venue.put("lng", mqrLng);
						
						venues.put(index, venue);
					}
					result.put("result", venues);
				}
			}
		}
		return result;
	}
}
