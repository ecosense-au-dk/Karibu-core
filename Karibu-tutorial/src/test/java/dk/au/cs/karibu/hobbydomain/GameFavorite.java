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

