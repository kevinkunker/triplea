package games.strategy.persistence.serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static games.strategy.test.Matchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import games.strategy.test.EqualityComparator;
import games.strategy.test.EqualityComparatorRegistry;

/**
 * A fixture for testing the basic aspects of proxy classes.
 *
 * @param <T> The type of the principal to be proxied.
 */
public abstract class AbstractProxyTestCase<T> {
  private final EqualityComparatorRegistry equalityComparatorRegistry =
      EqualityComparatorRegistry.newInstance(getEqualityComparators());

  private final Class<T> principalType;

  private final ProxyRegistry proxyRegistry = ProxyRegistry.newInstance(getProxyFactories());

  protected AbstractProxyTestCase(final Class<T> principalType) {
    checkNotNull(principalType);

    this.principalType = principalType;
  }

  /**
   * Asserts that the specified principals are equal.
   *
   * <p>
   * This implementation compares the two objects using the {@link games.strategy.test.Matchers#equalTo(Object)} matcher
   * with an equality comparator registry composed of the items from {@link #getEqualityComparators()}. Subclasses may
   * override and are not required to call the superclass implementation.
   * </p>
   *
   * @param expected The expected principal.
   * @param actual The actual principal.
   *
   * @throws AssertionError If the two principals are not equal.
   */
  protected void assertPrincipalEquals(final T expected, final T actual) {
    assertThat(actual, equalTo(expected).withEqualityComparatorRegistry(equalityComparatorRegistry));
  }

  /**
   * Creates a collection of principals whose capability to be persisted via a proxy will be tested.
   *
   * @return The collection of principals to be tested.
   */
  protected abstract Collection<T> createPrincipals();

  /**
   * Gets the collection of equality comparators required to compare two instances of the principal type for equality.
   *
   * <p>
   * This implementation returns an empty collection. Subclasses may override and are not required to call the
   * superclass implementation.
   * </p>
   *
   * @return The collection of equality comparators required to compare two instances of the principal type for
   *         equality.
   */
  protected Collection<EqualityComparator> getEqualityComparators() {
    return Arrays.asList();
  }

  /**
   * Gets the collection of proxy factories required for the principal to be persisted.
   *
   * @return The collection of proxy factories required for the principal to be persisted.
   */
  protected abstract Collection<ProxyFactory> getProxyFactories();

  private static Object readObject(final ByteArrayOutputStream baos) throws Exception {
    try (final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(is)) {
      return ois.readObject();
    }
  }

  private void writeObject(final ByteArrayOutputStream baos, final T obj) throws Exception {
    try (final ObjectOutputStream oos = new ProxyableObjectOutputStream(baos, proxyRegistry)) {
      oos.writeObject(obj);
    }
  }

  @Test
  public void shouldBeAbleToRoundTripPrincipal() throws Exception {
    final Collection<T> principals = createPrincipals();
    assertThat(principals, is(not(empty())));
    for (final T expected : principals) {
      assertThat(expected, is(not(nullValue())));
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();

      writeObject(baos, expected);
      final Object untypedActual = readObject(baos);

      assertThat(untypedActual, is(not(nullValue())));
      assertThat(untypedActual, is(instanceOf(principalType)));
      final T actual = principalType.cast(untypedActual);
      assertPrincipalEquals(expected, actual);
    }
  }
}
