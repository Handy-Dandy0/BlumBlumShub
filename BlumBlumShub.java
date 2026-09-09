import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class BlumBlumShub {

    /* ---------------------~ Important Stuff ~--------------------- */
    private static final boolean DEBUG_MODE = false;
    private static final String IS_COMPOSITE_REGEX = "^.?$|^(..+?)\\1+$"; /** ReGeX that can be checked for if a number is a composite number (not prime) */
    public static ArrayList<Integer> USED_P = new ArrayList<>();

    /* ----------------------~ Default Values ~---------------------- */
    private static final String IN_FILE = "input.txt";
    private static final String FILETYPE = IN_FILE.lastIndexOf('.') >= 0 ? IN_FILE.substring(IN_FILE.lastIndexOf('.')) : "";
    private static final String OUT_FILE = "Output".concat(FILETYPE);

    // Roughly the scalar of p, q, and seed. See findBBSArgs.
    public static final int MAGNITUDE = 10000;

    // Length of data to be encoded in bytes, should reflect actual size of file (i.e.: 7KB file -> ~7000 byte needed)
    private static final int BYTES = 7000;
    
    private static final int DEFAULT_P = 101747;
    private static final int DEFAULT_Q = 121019;
    private static final int DEFAULT_SEED = 1686765107;
    
    /* -------------------~ Less Important Stuff ~------------------- */
    public static final String ANS_RESET = "\u001B[0m";
    public static final String ANS_BLACK = "\u001B[30m";
    public static final String ANS_RED = "\u001B[31m";
    public static final String ANS_GREEN = "\u001B[32m";
    public static final String ANS_LIGHT_PINK = "\u001B[38;2;229;160;206m";
    /* 'ANS_COLOR's are not necessary - used for changing the color of text printed to console. If removed, just remove all instances of them :-) */


    public static void main(String[] args) throws FileNotFoundException {

        /* Example Use of functions:
         *  1. Either use the preset values of p, q, and seed or generate some using findBBSArgs()
         *  2. Call blumBlumShub, and store the output as you will need it to encode
         *  3. Run encodeInput, passing in data to be encoded or a file and the pseudorandom bytes. This will create a file with your encoded data.
         *  4. To then Decode, simply run decode input. *identical pseudorandom bytes are needed to decode as encode.*
         * 
         * Example use of generated arguments:
            int[] BBSargs = findBBSArgs(MAGNITUDE);
            byte[] randBin = blumBlumShub(BBSargs[0], BBSargs[1], BBSargs[2], BYTES);
        */

        if (!(new File(IN_FILE)).exists()) throw new FileNotFoundException(ANS_RED + "Input File Not Found '" + IN_FILE + "'. Check the definition for 'IN_FILE'." + ANS_RESET);

        byte[] randBin = blumBlumShub(DEFAULT_P, DEFAULT_Q, DEFAULT_SEED, BYTES);

        System.out.println("RandBin Length: " + randBin.length);

        byte[] encInp = encodeInput(IN_FILE, randBin);

        decodeInput(encInp, randBin, OUT_FILE);
    }

    public static byte[] encodeInput(String inputFileName, byte[] randBin) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(inputFileName));

            return encodeInput(data, randBin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encodeInput(File inputFile, byte[] randBin) {
        try {
            byte[] data = Files.readAllBytes(inputFile.toPath());

            if (data.length >= randBin.length) throw new IllegalStateException(ANS_RED + "Not random enough! (The array of pseudorandom bytes is smaller than the array of bytes to encode.)" + ANS_RESET);

            return encodeInput(data, randBin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Using Blum-Blum-Shub, encodes input data 
     * @param data An Array of Bytes containing binary to be encoded.
     * @param randBin An Array of Bytes containing psuedorandom binary, output from BBS. Must be at least the length of data
     * @return An encoded array of Bytes, from XORing data and randBin.
     */
    public static byte[] encodeInput(byte[] data, byte[] randBin) {
        if (data.length > randBin.length) throw new IllegalStateException(ANS_RED +  "Not random enough! (The array of pseudorandom bytes is smaller than the array of bytes to encode.)" + ANS_RESET);

        byte[] encodedInput = new byte[data.length];

        if (DEBUG_MODE) System.out.println("First five bytes encoded:");
        for (int i = 0; i < data.length; i ++) {
            encodedInput[i] = (byte)((data[i] & 0xFF) ^ (randBin[i] & 0xFF));
            if (i < 5 && DEBUG_MODE) {
                System.out.printf("\t%s ^ %s ==>  %s\n",
                    String.format("%8s",Integer.toBinaryString((int)data[i] & 0xFF)).replace(' ', '0'), 
                    String.format("%8s",Integer.toBinaryString((int)randBin[i] & 0xFF)).replace(' ', '0'),
                    String.format("%8s",Integer.toBinaryString(encodedInput[i] & 0xFF)).replace(' ', '0')
                );
            }
            try {
                Files.write(Paths.get("encodedBin".concat(FILETYPE)), encodedInput);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return encodedInput;
    }

    public static void decodeInput(String inputFileName, byte[] randBin, String outputFilename) {
        decodeInput(new File(inputFileName), randBin, outputFilename);
    } 

    public static void decodeInput(File inputFile, byte[] randBin, String outputFilename) {
        try {
            byte[] inputData = Files.readAllBytes(inputFile.toPath());
            decodeInput(inputData, randBin, outputFilename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void decodeInput(byte[] input, byte[] randBin, String outputFilename) {
        byte[] decodedBytes = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            decodedBytes[i] = (byte)((randBin[i] & 0xFF) ^ (input[i] & 0xFF));
        }
        try {
            Files.write(Paths.get(outputFilename), decodedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isPrime(int n) {
        return !Pattern.matches(IS_COMPOSITE_REGEX, "1".repeat(n));
    }

    public static int[] findBBSArgs(int magnitude) {
        return findBBSArgs(0, 0, 0, magnitude);
    }
    /**
     * Used to find suitable starting values for Blum-Blum-Shub. 
     * @param magnitude Defines the magnitude of the large primes to be used and the starting value of search for numbers that fit the prerequisites;
         *  p and q will be on the order of the magnitude squared, and seed values on the same order as the magnitude. The search for numbers will start at those numbers, respectively
     * @return Array containing three integers: p, q and seed respectively. All three are large prime numbers with their order depending on magnitude. 
     *  Additionally:
     *      p != q, p != seed, and q != seed;
     *      p and q are not factors of the seed and are strictly greater than 1;
     *      the modulo of both p and  q is congruent to 3 modulo 4;
     *      The GCD of (p-3)/2 and (q-3)/3 is small (chose == 10)
     */
    public static int[] findBBSArgs(int p, int q, int seed, int magnitude) {
        int i = 0;
        if (magnitude % 2 == 0) i++; // exclude even numbers; cannot be prime
        for (; i < magnitude + 10000; i += 2) {
            int val = i + magnitude;
            if (isPrime(val) && isPrime((val-1)/2) && val % 4 == 3) {
                if (p == 0 && !USED_P.contains(val)) {
                    p = val;
                    if (DEBUG_MODE) System.out.printf(ANS_LIGHT_PINK + "P" + ANS_BLACK + " IS SET TO: " + ANS_GREEN + "%d" + ANS_RESET + "\n\t%d mod 4 = %d\n",val , val, val%4);
                }
                else if (q == 0 && p != val && BigInteger.valueOf((p-3)/2).gcd(BigInteger.valueOf((q-3)/2)).compareTo(BigInteger.valueOf(1)) == 0) {
                    q = val;
                    if (DEBUG_MODE) System.out.printf(ANS_LIGHT_PINK + "Q" + ANS_BLACK + " IS SET TO: " + ANS_GREEN + "%d" + ANS_RESET + "\n\t%d mod 4 = %d\n", val, val, val%4);
                    break;
                }
            }
        }
        if (p == 0 || q == 0) {
            throw new IllegalStateException("Error: Failed to find valid values for p and q.");
        }
        for (i = 2 + seed; i < magnitude + 10000; i++) {
            int val = i + magnitude;
            if (
                (val % p != 0) && (val % q != 0) && BigInteger.valueOf(val).gcd(BigInteger.valueOf(p*q)).compareTo(BigInteger.valueOf(1)) == 0)
                {
                    seed = val;
                    if (DEBUG_MODE) {
                        System.out.printf(ANS_LIGHT_PINK + "SEED" + ANS_BLACK + " IS SET TO: " + ANS_GREEN + "%d" + ANS_RESET, seed);
                        System.out.printf("\n\t%d mod %d (p) = %d\n\t%d mod %d (q) = %d\n", p, seed, p % seed, q, seed, q % seed);
                    }
                    break;
                }
            }
        if (p < 2 && q < 2 && seed < 2) throw new IllegalStateException(ANS_RED + "Error in Finding Suitable Numbers." + ANS_RESET);
        return new int[]{p, q, seed};
    }

    /**
     * @param p Some large prime int
     * @param q Some large prime int 
     * @param seed An Integer that is co-prime to p*q.
     * @param bytes The number of bytes to be returned, if possible.
     * @return An array of bytes containing the 4 Most Significant Bits of each number generated at each step. </ul>
     * 
     * <!-- Below is formatting for JavaDoc --!>
     * <li><b>Necessary Assertions:</b>
     *      <ul>
     *          <li>p and q must be primes</li>
     *          <li>p modulo 4 must be congruent to q modulo 4 which must be congruent to 3 modulo 4</li>
     *          <li>seed must not be divisible by p or q</li>
     *      </ul>
     * </li>
     * <li><b>Important Notes:</b>
     *      <ul>
     *          <li>Ideally, p and q also have a small greatest common divisor of (p-3)/2 and (q-3)/2 </li>
     *          <li>Both p and q should be safe primes; that is to say (p-1)/2 is prime and (q-1)/2 are also prime.</li>
     *      </ul>
     * </li>
     */
    public static byte[] blumBlumShub(int p, int q, int seed, int bytes) {
        assert isPrime(p) : "Error: Invalid Arguments. p must be a prime number."; 
        assert isPrime(q) : "Error: Invalid Arguments. q must be a prime number."; 
        assert p % 4 == 3 : "Error: Invalid Arguments. p (modulo 4) must be equal to 3.";
        assert q % 4 == 3 : "Error: Invalid Arguments. q (modulo 4) must be equal to 3.";
        assert seed % p != 0 : "Error: Invalid Arguments. Seed cannot be a Co-Factor of p.";
        assert seed % q != 0 : "Error: Invalid Arguments. Seed cannot be a Co-Factor of q.";
        
        int n = p * q;
        ArrayList<Integer> numbers = new ArrayList<>();
        // It's recommended to generate only a maximum of sqrt(n) bits for any initialization
        for (int i = 0; i < bytes && i < Math.sqrt(n)/8; i++) {
            seed = ((seed * seed) % n);
            if (numbers.contains(seed)) {
                if (DEBUG_MODE) System.out.printf("RNG has fallen into a loop at %d steps. \n", i);
                break;
            }
            numbers.add(seed);
        }


        if (numbers.size() < bytes) {
            int[] newArgs = findBBSArgs(MAGNITUDE + (int) (numbers.size() * 9.4145213404 + bytes/23 - 24.314637569));
            byte[] additionalBytes = blumBlumShub(newArgs[0], newArgs[1], newArgs[2], bytes - numbers.size());
            for (byte b : additionalBytes) {
                numbers.add((int) b & 0xFF);
            }
        }
        byte[] z = new byte[bytes];
        for (int k = 0; k < bytes; k++) {
            z[k] = (byte)(numbers.remove(0).intValue() & 0b11111111);
        }
        return z;
    }
}