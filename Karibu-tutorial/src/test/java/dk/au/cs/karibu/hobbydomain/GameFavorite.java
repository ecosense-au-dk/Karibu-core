/*
 * Copyright 2013 Henrik Baerbak Christensen, Aarhus University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package dk.au.cs.karibu.hobbydomain;

/** A (silly) example of a domain class to be
 * handled by Karibu: Each object represents
 * a person name and his/her favorite computer game.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University 
 *  
*/ 
public  class GameFavorite { 
  private String name; 
  private String game; 

  public GameFavorite(String name, String favoriteComputerGame) { 
    this.name = name; 
    this.game = favoriteComputerGame; 
  } 

  public String getName() { 
    return name; 
  } 

  public String getGame() { 
    return game; 
  } 
} 

