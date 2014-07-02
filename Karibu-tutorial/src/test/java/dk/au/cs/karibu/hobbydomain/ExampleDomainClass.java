package dk.au.cs.karibu.hobbydomain;

  /* An example of a domain class */ 
public  class ExampleDomainClass { 
  private String name; 
  private String game; 

  public ExampleDomainClass(String name, String favoriteComputerGame) { 
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

