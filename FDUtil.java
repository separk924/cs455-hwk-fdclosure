import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;

/**
 * This utility class is not meant to be instantitated, and just provides some
 * useful methods on FD sets.
 * 
 * @author Seung Park
 * @version 10.26.22
 */
public final class FDUtil {

  /**
   * Resolves all trivial FDs in the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return a set of trivial FDs with respect to the given FDSet
   */
  public static FDSet trivial(final FDSet fdset) {
    // deep copy of the set
    FDSet copy = new FDSet(fdset);
    Set<Set<String>> theSet = new HashSet<>();
    Set<Set<String>> lefts = new HashSet<>();
    FDSet newFDFdSet = new FDSet();
    for (FD fd : copy) {
      Set<String> left = new HashSet<>();
      left = fd.getLeft();
      lefts.add(left);
      // Obtain the power set of each FD's left-hand attributes.
      theSet.addAll(powerSet(left));
      // For each element in the power set, create new FD and add to new FDSet.
      for (Set<String> inner : theSet) {
        if (!inner.isEmpty()) {
          FD func = new FD(left, inner);
          newFDFdSet.add(func);
        }
      }
    }
    return newFDFdSet;
  }

  /**
   * Augments every FD in the given set of FDs with the given attributes
   * 
   * @param fdset FD Set (Immutable)
   * @param attrs a set of attributes with which to augment FDs (Immutable)
   * @return a set of augmented FDs
   */
  public static FDSet augment(final FDSet fdset, final Set<String> attrs) {
    // deep copy of the set
    FDSet copy = new FDSet(fdset);
    for (FD fd : copy) {
      fd.addToLeft(attrs); // add attributes to left
      fd.addToRight(attrs); // add attributes to right
    }
    return copy;
  }

  /**
   * Exhaustively resolves transitive FDs with respect to the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return all transitive FDs with respect to the input FD set
   */
  public static FDSet transitive(final FDSet fdset) {

    FDSet copy = new FDSet(fdset);
    FDSet newD = new FDSet();
    FDSet theSet = new FDSet();

    for (FD fd : copy) {
      for (FD fd2 : copy) {
        if (!fd.equals(fd2)) {
          // if the transitive rule holds
          if (fd.getRight().equals(fd2.getLeft())) {
            FD elem = new FD(fd.getLeft(), fd2.getRight());
            theSet.add(elem);
            newD.add(elem);
          }
        }
      }
    }

    // union the dependencies where rule holds with the original set
    newD.addAll(copy);

    // if there were no new FDs added, return the set
    if (newD.size() == copy.size()) {
      return theSet;
    }

    // recurse on new set of FD's
    theSet = FDUtil.transitive(newD);

    return theSet;
  }

  /**
   * Gets all attributes of a set
   * 
   * @param fdset FD Set
   * @return set of attributes
   */
  public static Set<String> attributes(FDSet fdSet) {
    Set<String> theSet = new TreeSet<>();
    for (FD fd : fdSet) {
      theSet.addAll(fd.getLeft());
      theSet.addAll(fd.getRight());
    }
    return theSet;
  }

  /**
   * Generates the closure of the given FD Set
   * 
   * @param fdset (Immutable) FD Set
   * @return the closure of the input FD Set
   */
  public static FDSet fdSetClosure(final FDSet fdset) {
    FDSet copy = new FDSet(fdset); // deep copy of the given FDSet
    FDSet closure = new FDSet(); // set closure set
    FDSet added = new FDSet(); // changed set

    FDSet trivial = trivial(copy); // gets trivial
    FDSet augment = new FDSet(fdset); // copy for augmenting
    FDSet transitive = transitive(copy); // gets transitive

    // gets all attributes
    Set<Set<String>> attributes = FDUtil.powerSet(FDUtil.attributes(copy));
    // augments the set
    for (Set<String> set : attributes) {
      if (set.size() != 0) {
        augment.addAll(augment(copy, set));
      }
    }

    // unions the changed sets into added & set closure set
    closure.addAll(trivial);
    closure.addAll(augment);
    closure.addAll(transitive);
    added.addAll(trivial);
    added.addAll(augment);
    added.addAll(transitive);

    // unions original set with changed set
    added.addAll(copy);

    // if no changes were made, return the set closure
    if (copy.size() == added.size()) {
      return closure;
    }

    // recurse on the changed set
    closure = fdSetClosure(added);

    return closure;
  }

  /**
   * Generates the power set of the given set (that is, all subsets of
   * the given set of elements)
   * 
   * @param set Any set of elements (Immutable)
   * @return the power set of the input set
   */
  @SuppressWarnings("unchecked")
  public static <E> Set<Set<E>> powerSet(final Set<E> set) {

    // base case: power set of the empty set is the set containing the empty set
    if (set.size() == 0) {
      Set<Set<E>> basePset = new HashSet<>();
      basePset.add(new HashSet<>());
      return basePset;
    }

    // remove the first element from the current set
    E[] attrs = (E[]) set.toArray();
    set.remove(attrs[0]);

    // recurse and obtain the power set of the reduced set of elements
    Set<Set<E>> currentPset = FDUtil.powerSet(set);

    // restore the element from input set
    set.add(attrs[0]);

    // iterate through all elements of current power set and union with first
    // element
    Set<Set<E>> otherPset = new HashSet<>();
    for (Set<E> attrSet : currentPset) {
      Set<E> otherAttrSet = new HashSet<>(attrSet);
      otherAttrSet.add(attrs[0]);
      otherPset.add(otherAttrSet);
    }
    currentPset.addAll(otherPset);
    return currentPset;
  }
}