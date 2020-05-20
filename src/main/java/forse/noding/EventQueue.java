package forse.noding;


public interface EventQueue 
{
  void add(Event ev);
  Event peek();
  Event pop();
  boolean isEmpty();

}
