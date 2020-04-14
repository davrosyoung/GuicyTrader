package au.com.livewire;

public interface Translator<R,S> {
  /**
   * Translate
   * @param candidate
   * @return
   */
  public R from(S candidate);

  public S to(R result);
}
