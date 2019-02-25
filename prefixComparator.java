import java.util.*;
import java.io.*;
import java.net.*;

/*
 * prefixComparator
 *
 * this class is used when sorting an array in Java.
 */
class prefixComparator implements Comparator<prefix> {
	public int compare(prefix a, prefix b) {
		if (a.len > b.len)
			return 1;
		else if (a.len == b.len)
			return 0;
		else
			return -1;
		/*
		 * XXX: return a value such that the array is ordered from most specific
		 * to least specific. take a look at the documentation at
		 *
		 * http://docs.oracle.com/javase/7/docs/api/java/util/Comparator.html
		 *
		 * on how the return value from this method will impact sort order. make
		 * sure the longest prefixes are sorted so that they come first!
		 */
	}
};

/*
 * prefix
 *
 * this class holds details of a prefix: the network address, the prefix length,
 * and details of the autonomous systems that announce it.
 *
 */
class prefix {
	public int[] net = { 0, 0, 0, 0 };
	public int len;
	public String asn;

	public prefix(String net, int len, String asn) {
		String delim = "[.]";
		String[] netToks = net.split(delim);
		this.len = len;
		this.asn = asn;
		for (int i = 0; i < 4; i++)
			this.net[i] = Integer.parseInt(netToks[i]);

		/*
		 * XXX: initialise the object given the inputs. break the network ID
		 * into four integers.
		 */
	}

	public String toString() {
		/* pretty print out of the prefix! my lecturer is kind! */
		return net[0] + "." + net[1] + "." + net[2] + "." + net[3] + "/" + len;
	}

	/*
	 * match
	 *
	 * given an address, determine if it is found in this prefix structure or
	 * not.
	 */
	public boolean match(String addr) {
		int[] mask = { 0x80, 0xC0, 0xE0, 0xF0, 0xF8, 0xFC, 0xFE, 0xFF };
		int i;

		/*
		 * XXX: break up the address passed in as a string
		 */
		int[] othernet = new int[4];
		String delim = "[.]";
		// System.out.println("ADDRESS: "+addr);
		String[] netToks = addr.split(delim);
		for (int j = 0; j < 4; j++)
			othernet[j] = Integer.parseInt(netToks[j]);
		boolean match = true;
		int currentMaskAmt = 0;
		for (i = 0; i < 4; i++) {
			int maskAmt = 0;
			while (currentMaskAmt < (len-1)) {
				currentMaskAmt++;
				maskAmt++;
				if (maskAmt >= 7)
					break;
			}
			// System.out.println("MASK: "+mask[maskAmt]);
			// System.out.println("IP SEG: "+net[i]);
			// System.out.println("OTHER IP SEG: "+othernet[i]);
			if ((othernet[i] & mask[maskAmt]) != (net[i] & mask[maskAmt])) {
				match = false;
				break;
			}
			/*
			 * XXX: compare up to four different values in the dotted quad,
			 * (i.e. enough to cover this.len) to determine if this address is a
			 * match or not
			 */
		}
		return match;
	}
};

class ip2as {
	public static void main(String args[]) {
		if (args.length < 3) {
			/* always check the input to the program! */
			System.err.println("usage: ip2as <prefixes> <asnames> [ip0 ip1 .. ipN]");
			return;
		}

		/* read the prefix list into a list */
		ArrayList<prefix> list = new ArrayList<prefix>();
		try {
			BufferedReader file = new BufferedReader(new FileReader(args[0]));
			String line;

			while ((line = file.readLine()) != null) {
				/* XXX: add code to parse the ip2as line */
				String net, ases;
				int len;
				String delim = "[/ ]";
				String[] tokens = line.split(delim);
				net = tokens[0];
				len = Integer.parseInt(tokens[1]);
				ases = tokens[2];
				/* create a new prefix object and stuff it in the list */
				if (len > 7 && len < 29) {
					prefix pf = new prefix(net, len, ases);
					list.add(pf);
				}
			}
			file.close();
		} catch (FileNotFoundException e) {
			System.err.println("could not open prefix file " + args[0]);
			return;
		} catch (IOException e) {
			System.err.println("error reading prefix file " + args[0] + ": " + e);
		}

		/*
		 * take the list of prefixes and transform it into a sorted array i'd
		 * like to thank my lecturer for giving me this code.
		 */
		prefix[] x = new prefix[list.size()];
		list.toArray(x);
		Arrays.sort(x, new prefixComparator());

		/*
		 * read in the asnames file so that we can report the network's name
		 * with its ASN
		 */
		HashMap<String, String> asMap = new HashMap<String, String>();
		BufferedReader file;
		try {
			file = new BufferedReader(new FileReader(args[1]));
			String line;

			while ((line = file.readLine()) != null) {
				String[] tokens = line.split("[ ]", 2);
				// int AS = Integer.parseInt(line.substring(0, 2));
				// line = line.substring(2);
				asMap.put(tokens[0], tokens[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * for all IP addresses supplied on the command line, print out the
		 * corresponding ASes that announce a corresponding prefix, as well as
		 * their names. if there is no corresponding prefix, print the IP
		 * address and then say no corresponding prefix.
		 */
		for (int i = 2; i < args.length; i++) {
			int matched = 0;

			/*
			 * x contains the sorted array of prefixes, organised longest to
			 * shortest prefix match
			 */
			for (int j = 0; j < x.length; j++) {
				prefix p = x[j];
				if (p.match(args[i]) == true) {
					matched++;
					System.out.println(args[i] + " --- " + p.asn + " " + asMap.get(p.asn));
				}
				/*
				 * XXX: check if this prefix matches the IP address passed in
				 */
			}
			if (matched == 0) {
				System.out.println(args[i] + " --- NO MATCH FOUND");
			}
			/*
			 * XXX: print something out if it was not matched
			 */
		}
		return;
	}
};