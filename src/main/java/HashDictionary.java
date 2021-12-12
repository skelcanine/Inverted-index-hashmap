import java.util.ArrayList;
import java.util.List;

public class HashDictionary<K, V> implements DictionaryInterface<K, V> {
         // The dictionary:
         private int numberOfEntries;
         private static final int DEFAULT_CAPACITY = 5; // Must be prime
         private static final int MAX_CAPACITY = 300000;
         public static int enlargedTimes=0;


         // The hash table:
         private ArrayList<TableEntry<K,V>>[] hashTable;
         private int tableSize; // Must be prime
         private static final int MAX_SIZE = 2 * MAX_CAPACITY;
         private boolean initialized = false;
         private double MAX_LOAD_FACTOR; // Fraction of hash table
         private static final double DEFAULT_MAX_LOAD_FACTOR = 0.5;
         private static final int temp_integer = 96;
         private static final int PAF_CONSTANT=31;
         private static hashFunctionType HASH_FUNCTION_TYPE;
         private static collisionHandlingType COLLISION_HANDLING_TYPE;
         private static long collisionCount = 0;




         // enumrators for hashtype and collision handling type
         public enum hashFunctionType {SSF,PAF};
         public enum collisionHandlingType{LP,DH};

         // Default constructor
         public HashDictionary() {

                 this(DEFAULT_CAPACITY,DEFAULT_MAX_LOAD_FACTOR,hashFunctionType.SSF,collisionHandlingType.LP); // Call next constructor
         } // end default constructor
         //Constructor with parameter
         public HashDictionary(int initialCapacity, double loadFactor,hashFunctionType hashFunction,collisionHandlingType CollisionType) {
                 int kpz =1;
                 kpz++;
                 checkCapacity(initialCapacity);
                 numberOfEntries = 0; // Dictionary is empty
                 MAX_LOAD_FACTOR = loadFactor; // Set load factor
                 HASH_FUNCTION_TYPE = hashFunction; // Set hash function type
                 COLLISION_HANDLING_TYPE = CollisionType; // Set collision type

                 // Set up hash table:
                 // Initial size of hash table is same as initialCapacity if it is prime;
                 // otherwise increase it until it is prime size
                 int tableSize = getNextPrime(initialCapacity);
                 checkSize(tableSize); // Check for max array size

                 // The cast is safe because the new array contains null entries
                 @SuppressWarnings("unchecked")
                 ArrayList<TableEntry<K,V>>[] temp = (ArrayList<TableEntry<K,V>>[]) new ArrayList[tableSize];
                 hashTable = temp;
                 initialized = true;
         }
         //Return next prime
         private int getNextPrime(int initialCapacity) {
             int prime = initialCapacity;
             boolean found = false;
             // Loop continuously until isPrime returns
             // true for a number greater than n
             while (!found)
             {
                 prime++;
                 if (isPrime(prime))
                     found = true;
             }
             return prime;
         }

         //Check if given argument is prime
        static boolean isPrime(int n)
        {
            // This is checked so that we can skip
            // middle five numbers in below loop
            if (n % 2 == 0 || n % 3 == 0) return false;

            for (int i = 5; i * i <= n; i = i + 6)
                if (n % i == 0 || n % (i + 2) == 0)
                    return false;

            return true;
        }


         //Check table size constraint
         private void checkSize(int tableSize) {
             assert  tableSize<MAX_SIZE;
         }

         //Check if capacity maximum reached
         private void checkCapacity(int capacity)
         {
                 if (capacity > MAX_CAPACITY)
                         throw new IllegalStateException("Attempt to create a bag whose " +
                                 "capacity exeeds allowed " +
                                 "maximum of " + MAX_CAPACITY);
         } //

         //Returns locations and occurrence counts of given key in each document
         public ArrayList<String> get(K key)
         {      ArrayList<String> x = new ArrayList<>();
                 checkInitialization();
                 String result = "";

                 int index;
                 if(HASH_FUNCTION_TYPE==hashFunctionType.SSF)
                     index = getHashSSF(key);
                 else
                     index = getHashPAF(key);
                 if(COLLISION_HANDLING_TYPE==collisionHandlingType.LP)
                     index = locate(index, key);
                 else
                     index = locateDH(index, key);

                 if (index != -1) {

                     for(int ix =0;ix<hashTable[index].size();ix++) {
                         result =String.valueOf(hashTable[index].get(ix).getCount())+"-" + (String)hashTable[index].get(ix).getValue();
                         x.add(result);// Key found; get value

                     }
                 }
                 return x;
         }

     //Check if structure is empty
    @Override
    public boolean isEmpty() {
        return numberOfEntries==0;
    }
    //Returns size of entries
    @Override
    public int getSize() {
        return numberOfEntries;
    }


    //Returns collision count
    public static long getCollisionCount() {
        return collisionCount;
    }
    //Returns hash of a key in a way that Simple Summation Function
    private int getHashSSF(K key) {
             int sum=0;
             int temp;
             String strx = key.toString();

        for (int i = 0; i < strx.length(); i++) {

            temp=((int) strx.charAt(i))-temp_integer;
            sum +=temp;
        }
             return sum % hashTable.length;
    }
    //Returns hash of a key in a way that Polynomial Accumulation Function
    private int getHashPAF(K key) {
        int sum=0;
        int tempcharint;
        int temptotal;
        String strx = key.toString();
        for (int i = 0; i < strx.length(); i++)
        {
            tempcharint = ((int) strx.charAt(i))-temp_integer;
            temptotal= (int) (tempcharint*Math.pow(PAF_CONSTANT,strx.length()-i-1))%hashTable.length;
            sum +=temptotal;

        }
            int result = sum%hashTable.length;
            if (result<0)
                result=result+hashTable.length;
        return result;
    }
    //Returns second hash function
    private int secondHash(int key)
    {   int result;

        result = (7-(key%7));

        if (result<0)
        {
            result= result+7;
        }

        return result;
    }






    // Check if initialization true
    private void checkInitialization() {
             assert initialized;
    }

    //Remove given key from structure
    public void remove(K key)
         {
                 checkInitialization();
                 int removedcount=0;
                 int index;
                 if(HASH_FUNCTION_TYPE==hashFunctionType.SSF)
                     index = getHashSSF(key);
                 else
                     index = getHashPAF(key);

                 if(COLLISION_HANDLING_TYPE==collisionHandlingType.LP)
                     index = locate(index, key);
                 else
                    index = locateDH(index, key);

                 if (index != -1)
                 { // Key found; flag entry as removed and return its value
                         for (int i = 0;i< hashTable[index].size();i++)
                         {
                             removedcount += hashTable[index].get(i).getCount();
                         }
                         hashTable[index].clear();
                         numberOfEntries=numberOfEntries-removedcount;
                 } // end if
// Else key not found; return null

         }
        //Locate given key with index with linear probing
         private int locate(int index, K key)
         {
                 boolean found = false;

                 while ( !found && (hashTable[index] != null) )
                 {          String s =(String) hashTable[index].get(0).getKey();
                            int ix = hashTable[index].size();
                         if ( hashTable[index] != null && key.equals(hashTable[index].get(0).getKey()) )
                                 found = true; // Key found
                         else // Follow probe sequence
                                 index = (index + 1) % hashTable.length; // Linear probing
                 } // end while

                 int result = -1;
                 if (found)
                         result = index;
                 return result;
         }


    //Locate given key with index with double hashing
    private int locateDH(int index, K key)
        {   int mainindex = index;
            int probecounter=0;
            int secondhash=secondHash(index);

            boolean found = false;
            while ( !found && (hashTable[index] != null) )
            {
                if ( hashTable[index] != null && key.equals(hashTable[index].get(0).getKey()) ) {
                    found = true; // Key found
                }
                else { // Follow probe sequence
                    index = (mainindex + probecounter*secondhash) % hashTable.length;
                    probecounter++;// Double Hashing
                }
            } // end while

            int result = -1;
            if (found)
                result = index;
            return result;
        }
        //Inserting given key and value to structure with designated parameters of structure
         public int put(K key, V value)
         {
                 checkInitialization();
                 if ((key == null) || (value == null))
                         throw new IllegalArgumentException();
                 else
                 {
                         int oldValue; // Value to return
                         oldValue = 0;


                     int index;
                     if(HASH_FUNCTION_TYPE==hashFunctionType.SSF)
                         index = getHashSSF(key);
                     else
                         index = getHashPAF(key);

                     if(COLLISION_HANDLING_TYPE==collisionHandlingType.LP)
                         index = probe(index, key);
                     else
                         index = probeDH(index, key);

                        // Check for and resolve collision
                // Assertion: index is within legal range for hashTable
                         assert (index >= 0) && (index < hashTable.length);
                         if ( (hashTable[index] == null))
                         { // Key not found, so insert new entry
                                ArrayList<TableEntry<K,V>> x = new ArrayList<>();
                                 hashTable[index]=x;
                                 x.add(new TableEntry<K, V>(key, value));
                                 numberOfEntries++;
                                 oldValue = 1;
                         }else if(hashTable[index].size()==0){
                                 hashTable[index].add(new TableEntry<K, V>(key, value));
                                 numberOfEntries++;
                                 oldValue = 1;

                         }
                         else
                         { // Key found; get old value and increment count
                                boolean valueExist = false;
                                 for(int k=0;k<hashTable[index].size();k++)
                                 {
                                  TableEntry x =   hashTable[index].get(k);

                                  if(x.getKey().equals(key) && x.getValue().equals(value))
                                  {   valueExist = true;
                                      hashTable[index].get(k).updateCount();
                                      oldValue = hashTable[index].get(k).getCount();

                                      break;
                                  }
                                 }
                                 if (valueExist==false) {
                                     hashTable[index].add(new TableEntry<K, V>(key, value));
                                     oldValue=1;
                                 }
                         } // end if
// Ensure that hash table is large enough for another add
                         if (isHashTableTooFull())
                                 enlargeHashTable();
                         return oldValue;
                 } // end if
         }
    //Probe for key with index with linear probing
    private int probe(int index, K key)
    {
        boolean found = false;
        int removedStateIndex = -1; // Index of first location in removed state
        while ( !found && (hashTable[index] != null) )
        {
            //hashTable[index].size()!=0
            if (hashTable[index].size()!=0)
            {
                if (key.equals(hashTable[index].get(0).getKey()))
                    found = true; // Key found
                else { // Follow probe sequence
                    index = (index + 1) % hashTable.length; // Linear probing
                    collisionCount++;
                }
            }
            else // Skip entries that were removed
            {
// Save index of first location in removed state
                if (removedStateIndex == -1)
                    removedStateIndex = index;
                index = (index + 1) % hashTable.length; // Linear probing
            } // end if
        } // end while
// Assertion: Either key or null is found at hashTable[index]
        if (found || (removedStateIndex == -1) )
            return index; // Index of either key or null
        else
            return removedStateIndex; // Index of an available location
    }

    //Probe for key with index with double hashing
    private int probeDH(int index, K key)
    {
        int mainindex = index;
        int probecounter=0;
        int secondhash=secondHash(index);

        boolean found = false;
        int removedStateIndex = -1; // Index of first location in removed state
        while ( !found && (hashTable[index] != null) )
        {
            //hashTable[index].size()!=0
            if (hashTable[index].size()!=0)
            {
                if (key.equals(hashTable[index].get(0).getKey())){
                    found = true;} // Key found
                else { // Follow probe sequence
                    index = (mainindex + probecounter*secondhash) % hashTable.length;
                    probecounter++; // Linear probing
                    collisionCount++;
                }
            }
            else // Skip entries that were removed
            {
// Save index of first location in removed state
                if (removedStateIndex == -1)
                    removedStateIndex = index;
                index = (mainindex + probecounter*secondhash) % hashTable.length;
                probecounter++; // Linear probing
            } // end if
        } // end while
// Assertion: Either key or null is found at hashTable[index]
        if (found || (removedStateIndex == -1) )
            return index; // Index of either key or null
        else
            return removedStateIndex; // Index of an available location
    }

    //Check tables fullness state
    private boolean isHashTableTooFull() {
            double x = ((double)numberOfEntries/ hashTable.length);
             return x>= MAX_LOAD_FACTOR;
    }
    //If structure is small for given load factor enlarge it
    private void enlargeHashTable()
         {          enlargedTimes++;
                 ArrayList<TableEntry<K,V>>[] oldTable = hashTable.clone();
                 int oldSize = hashTable.length;
                 int newSize = getNextPrime(oldSize + oldSize);
                 checkSize(newSize);
// The cast is safe because the new array contains null entries
                 @SuppressWarnings("unchecked")
                 ArrayList<TableEntry<K,V>>[] temp = (ArrayList<TableEntry<K,V>>[]) new ArrayList[newSize];

                 hashTable = temp;
                 numberOfEntries = 0;
                 collisionCount = 0;
                // Reset number of dictionary entries, since
// it will be incremented by add during rehash
// Rehash dictionary entries from old array to the new and bigger
// array; skip both null locations and removed entries
                 for (int index = 0; index < oldSize; index++)
                 {
                         if ( (oldTable[index] != null) && oldTable[index]!=null ) {

                             for (int i = 0; i < oldTable[index].size(); i++) {
                                 for (int k = 0; k < oldTable[index].get(i).getCount(); k++){
                                     this.put(oldTable[index].get(i).getKey(), oldTable[index].get(i).getValue());
                                 }
                                 int bas = 1;
                             }
                         }

                         }

                         // end for
         }
    @Override
    public void clear() {

    }
//Entry class for structure
         private static class TableEntry<S, T> {
                 private S key;
                 private T value;
                 private int count;

                 private TableEntry(S searchKey, T dataValue) {
                         key = searchKey;
                         value = dataValue;
                         count=1;
                 }

                 public S getKey() {
                         return key;
                 }

                 public T getValue() {
                         return value;
                 }

                 public int getCount() {
                     return count;
                 }

                 public void updateCount(){
                     this.count++;
                 }


                 public void setValue(T value) {
                         this.value = value;
                 }

         }


 }

