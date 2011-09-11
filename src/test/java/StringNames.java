/*
 Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

   - Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.

   - Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.

   - Neither the name of Sun Microsystems nor the names of its
     contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * Universal mangling rules for the JVM.
 *
 * @author John Rose
 * @version 1.1, 03/03/08
 * @see http://blogs.sun.com/jrose/entry/symbolic_freedom_in_the_vm
 *
 * <h3>Avoiding Dangerous Characters </h3>
 *
 * <p>
 * The JVM defines a very small set of characters which are illegal
 * in name spellings.  We will slightly extend and regularize this set
 * into a group of <cite>dangerous characters</cite>.
 * These characters will then be replaced, in mangled names, by escape sequences.
 * In addition, accidental escape sequences must be further escaped.
 * Finally, a special prefix will be applied if and only if
 * the mangling would otherwise fail to begin with the escape character.
 * This happens to cover the corner case of the null string,
 * and also clearly marks symbols which need demangling.
 * </p>
 * <p>
 * Dangerous characters are the union of all characters forbidden
 * or otherwise restricted by the JVM specification,
 * plus their mates, if they are brackets
 * (<code><big><b>[</b></big></code> and <code><big><b>]</b></big></code>,
 * <code><big><b>&lt;</b></big></code> and <code><big><b>&gt;</b></big></code>),
 * plus, arbitrarily, the colon character <code><big><b>:</b></big></code>.
 * There is no distinction between type, method, and field names.
 * This makes it easier to convert between mangled names of different
 * types, since they do not need to be decoded (demangled).
 * </p>
 * <p>
 * The escape character is backslash <code><big><b>\</b></big></code>
 * (also known as reverse solidus).
 * This character is, until now, unheard of in bytecode names,
 * but traditional in the proposed role.
 *
 * </p>
 * <h3> Replacement Characters </h3>
 *
 *
 * <p>
 * Every escape sequence is two characters
 * (in fact, two UTF8 bytes) beginning with
 * the escape character and followed by a
 * <cite>replacement character</cite>.
 * (Since the replacement character is never a backslash,
 * iterated manglings do not double in size.)
 * </p>
 * <p>
 * Each dangerous character has some rough visual similarity
 * to its corresponding replacement character.
 * This makes mangled symbols easier to recognize by sight.
 * </p>
 * <p>
 * The dangerous characters are
 * <code><big><b>/</b></big></code> (forward slash, used to delimit package components),
 * <code><big><b>.</b></big></code> (dot, also a package delimiter),
 * <code><big><b>;</b></big></code> (semicolon, used in signatures),
 * <code><big><b>$</b></big></code> (dollar, used in inner classes and synthetic members),
 * <code><big><b>&lt;</b></big></code> (left angle),
 * <code><big><b>&gt;</b></big></code> (right angle),
 * <code><big><b>[</b></big></code> (left square bracket, used in array types),
 * <code><big><b>]</b></big></code> (right square bracket, reserved in this scheme for language use),
 * and <code><big><b>:</b></big></code> (colon, reserved in this scheme for language use).
 * Their replacements are, respectively,
 * <code><big><b>|</b></big></code> (vertical bar),
 * <code><big><b>,</b></big></code> (comma),
 * <code><big><b>?</b></big></code> (question mark),
 * <code><big><b>%</b></big></code> (percent),
 * <code><big><b>^</b></big></code> (caret),
 * <code><big><b>_</b></big></code> (underscore), and
 * <code><big><b>{</b></big></code> (left curly bracket),
 * <code><big><b>}</b></big></code> (right curly bracket),
 * <code><big><b>!</b></big></code> (exclamation mark).
 * In addition, the replacement character for the escape character itself is
 * <code><big><b>-</b></big></code> (hyphen),
 * and the replacement character for the null prefix is
 * <code><big><b>=</b></big></code> (equal sign).
 * </p>
 * <p>
 * An escape character <code><big><b>\</b></big></code>
 * followed by any of these replacement characters
 * is an escape sequence, and there are no other escape sequences.
 * An equal sign is only part of an escape sequence
 * if it is the second character in the whole string, following a backslash.
 * Two consecutive backslashes do <em>not</em> form an escape sequence.
 * </p>
 * <p>
 * Each escape sequence replaces a so-called <cite>original character</cite>
 * which is either one of the dangerous characters or the escape character.
 * A null prefix replaces an initial null string, not a character.
 * </p>
 * <p>
 * All this implies that escape sequences cannot overlap and may be
 * determined all at once for a whole string.  Note that a spelling
 * string can contain <cite>accidental escapes</cite>, apparent escape
 * sequences which must not be interpreted as manglings.
 * These are disabled by replacing their leading backslash with an
 * escape sequence (<code><big><b>\-</b></big></code>).  To mangle a string, three logical steps
 * are required, though they may be carried out in one pass:
 * </p>
 * <ol>
 *   <li>In each accidental escape, replace the backslash with an escape sequence
 * (<code><big><b>\-</b></big></code>).</li>
 *   <li>Replace each dangerous character with an escape sequence
 * (<code><big><b>\|</b></big></code> for <code><big><b>/</b></big></code>, etc.).</li>
 *   <li>If the first two steps introduced any change, <em>and</em>
 * if the string does not already begin with a backslash, prepend a null prefix (<code><big><b>\=</b></big></code>).</li>
 * </ol>
 *
 * To demangle a mangled string that begins with an escape,
 * remove any null prefix, and then replace (in parallel)
 * each escape sequence by its original character.
 * <p>Spelling strings which contain accidental
 * escapes <em>must</em> have them replaced, even if those
 * strings do not contain dangerous characters.
 * This restriction means that mangling a string always
 * requires a scan of the string for escapes.
 * But then, a scan would be required anyway,
 * to check for dangerous characters.
 *
 * </p>
 * <h3> Nice Properties </h3>
 *
 * <p>
 * If a bytecode name does not contain any escape sequence,
 * demangling is a no-op:  The string demangles to itself.
 * Such a string is called <cite>self-mangling</cite>.
 * Almost all strings are self-mangling.
 * In practice, to demangle almost any name &ldquo;found in nature&rdquo;,
 * simply verify that it does not begin with a backslash.
 * </p>
 * <p>
 * Mangling is a one-to-one function, while demangling
 * is a many-to-one function.
 * A mangled string is defined as <cite>validly mangled</cite> if
 * it is in fact the unique mangling of its spelling string.
 * Three examples of invalidly mangled strings are <code><big><b>\=foo</b></big></code>,
 * <code><big><b>\-bar</b></big></code>, and <code><big><b>baz\!</b></big></code>, which demangle to <code><big><b>foo</b></big></code>, <code><big><b>\bar</b></big></code>, and
 * <code><big><b>baz\!</b></big></code>, but then remangle to <code><big><b>foo</b></big></code>, <code><big><b>\bar</b></big></code>, and <code><big><b>\=baz\-!</b></big></code>.
 * If a language back-end or runtime is using mangled names,
 * it should never present an invalidly mangled bytecode
 * name to the JVM.  If the runtime encounters one,
 * it should also report an error, since such an occurrence
 * probably indicates a bug in name encoding which
 * will lead to errors in linkage.
 * However, this note does not propose that the JVM verifier
 * detect invalidly mangled names.
 * </p>
 * <p>
 * As a result of these rules, it is a simple matter to
 * compute validly mangled substrings and concatenations
 * of validly mangled strings, and (with a little care)
 * these correspond to corresponding operations on their
 * spelling strings.
 * </p>
 * <ul>
 *   <li>Any prefix of a validly mangled string is also validly mangled,
 * although a null prefix may need to be removed.</li>
 *   <li>Any suffix of a validly mangled string is also validly mangled,
 * although a null prefix may need to be added.</li>
 *   <li>Two validly mangled strings, when concatenated,
 * are also validly mangled, although any null prefix
 * must be removed from the second string,
 * and a trailing backslash on the first string may need escaping,
 * if it would participate in an accidental escape when followed
 * by the first character of the second string.</li>
 * </ul>
 * <p>If languages that include non-Java symbol spellings use this
 * mangling convention, they will enjoy the following advantages:
 * </p>
 * <ul>
 *   <li>They can interoperate via symbols they share in common.</li>
 *   <li>Low-level tools, such as backtrace printers, will have readable displays.</li>
 *   <li>Future JVM and language extensions can safely use the dangerous characters
 * for structuring symbols, but will never interfere with valid spellings.</li>
 *   <li>Runtimes and compilers can use standard libraries for mangling and demangling.</li>
 *   <li>Occasional transliterations and name composition will be simple and regular,
 * for classes, methods, and fields.</li>
 *   <li>Bytecode names will continue to be compact.
 * When mangled, spellings will at most double in length, either in
 * UTF8 or UTF16 format, and most will not change at all.</li>
 * </ul>
 *
 *
 * <h3> Suggestions for Human Readable Presentations </h3>
 *
 *
 * <p>
 * For human readable displays of symbols,
 * it will be better to present a string-like quoted
 * representation of the spelling, because JVM users
 * are generally familiar with such tokens.
 * We suggest using single or double quotes before and after
 * symbols which are not valid Java identifiers,
 * with quotes, backslashes, and non-printing characters
 * escaped as if for literals in the Java language.
 * </p>
 * <p>
 * For example, an HTML-like spelling
 * <code><big><b>&lt;pre&gt;</b></big></code> mangles to
 * <code><big><b>\^pre\_</b></big></code> and could
 * display more cleanly as
 * <code><big><b>'&lt;pre&gt;'</b></big></code>,
 * with the quotes included.
 * Such string-like conventions are <em>not</em> suitable
 * for mangled bytecode names, in part because
 * dangerous characters must be eliminated, rather
 * than just quoted.  Otherwise internally structured
 * strings like package prefixes and method signatures
 * could not be reliably parsed.
 * </p>
 * <p>
 * In such human-readable displays, invalidly mangled
 * names should <em>not</em> be demangled and quoted,
 * for this would be misleading.  Likewise, JVM symbols
 * which contain dangerous characters (like dots in field
 * names or brackets in method names) should not be
 * simply quoted.  The bytecode names
 * <code><big><b>\=phase\,1</b></big></code> and
 * <code><big><b>phase.1</b></big></code> are distinct,
 * and in demangled displays they should be presented as
 * <code><big><b>'phase.1'</b></big></code> and something like
 * <code><big><b>'phase'.1</b></big></code>, respectively.
 * </p>
 */
public class StringNames {
    private StringNames() { }  // static only class

    /** Given a source name, produce the corresponding bytecode name.
     */
    public static String toBytecodeName(String s) {
        String bn = mangle(s);
        assert(bn == s || looksMangled(bn)) : bn;
        assert(s.equals(toSourceName(bn))) : s;
        return bn;
    }

    /** Given a bytecode name, produce the corresponding source name.
     */
    public static String toSourceName(String s) {
        assert(isSafeBytecodeName(s)) : s;
        String sn = s;
        if (looksMangled(s)) {
            sn = demangle(s);
            assert(s.equals(mangle(sn))) : s+" => "+sn+" => "+mangle(sn);
        }
        return sn;
    }

    /** Given a bytecode name, produce the corresponding display name.
     *  This is the source name, plus quotes if needed.
     */
    public static String toDisplayName(String s) {
        if (isSafeBytecodeName(s)) {
            boolean isuid = Character.isUnicodeIdentifierStart(s.charAt(0));
            for (int i = 1, slen = s.length(); i < slen; i++) {
                if (!Character.isUnicodeIdentifierPart(s.charAt(0)))
                    { isuid = false; break; }
            }
            if (isuid)
                return s;
            String ss = toSourceName(s);
            if (s.equals(toBytecodeName(ss)))
                return quoteDisplay(ss);
        }
        // Try to demangle a prefix, up to the first dangerous char.
        int dci = indexOfDangerousChar(s, 0);
        if (dci > 0) {
            // At least try to demangle a prefix.
            String p = s.substring(0, dci);
            String ps = toSourceName(p);
            if (p.equals(toBytecodeName(ps))) {
                String t = s.substring(dci+1);
                return quoteDisplay(toSourceName(p)) + s.charAt(dci) + (t.equals("") ? "" : toDisplayName(t));
            }
        }
        return "?"+quoteDisplay(s);
    }
    private static String quoteDisplay(String s) {
        // TO DO:  Replace wierd characters in s by C-style escapes.
        return "'"+s.replaceAll("['\\\\]", "\\\\$0")+"'";
    }

    private static boolean isSafeBytecodeName(String s) {
        if (s.length() == 0)  return false;
        // check occurrences of each DANGEROUS char
        for (char xc : DANGEROUS_CHARS_A) {
            if (xc == ESCAPE_C)  continue;  // not really that dangerous
            if (s.indexOf(xc) >= 0)  return false;
        }
        return true;
    }

    private static boolean looksMangled(String s) {
        return s.charAt(0) == ESCAPE_C;
    }

    private static String mangle(String s) {
        if (s.length() == 0)
            return NULL_ESCAPE;

        // build this lazily, when we first need an escape:
        StringBuilder sb = null;

        for (int i = 0, slen = s.length(); i < slen; i++) {
            char c = s.charAt(i);

            boolean needEscape = false;
            if (c == ESCAPE_C) {
                if (i+1 < slen) {
                    char c1 = s.charAt(i+1);
                    if ((i == 0 && c1 == NULL_ESCAPE_C)
                        || c1 != originalOfReplacement(c1)) {
                        // an accidental escape
                        needEscape = true;
                    }
                }
            } else {
                needEscape = isDangerous(c);
            }

            if (!needEscape) {
                if (sb != null)  sb.append(c);
                continue;
            }

            // build sb if this is the first escape
            if (sb == null) {
                sb = new StringBuilder(s.length()+10);
                // mangled names must begin with a backslash:
                if (s.charAt(0) != ESCAPE_C && i > 0)
                    sb.append(NULL_ESCAPE);
                // append the string so far, which is unremarkable:
                sb.append(s.substring(0, i));
            }

            // rewrite \ to \-, / to \|, etc.
            sb.append(ESCAPE_C);
            sb.append(replacementOf(c));
        }

        if (sb != null)   return sb.toString();

        return s;
    }

    private static String demangle(String s) {
        // build this lazily, when we first meet an escape:
        StringBuilder sb = null;

        int stringStart = 0;
        if (s.startsWith(NULL_ESCAPE))
            stringStart = 2;

        for (int i = stringStart, slen = s.length(); i < slen; i++) {
            char c = s.charAt(i);

            if (c == ESCAPE_C && i+1 < slen) {
                // might be an escape sequence
                char rc = s.charAt(i+1);
                char oc = originalOfReplacement(rc);
                if (oc != rc) {
                    // build sb if this is the first escape
                    if (sb == null) {
                        sb = new StringBuilder(s.length());
                        // append the string so far, which is unremarkable:
                        sb.append(s.substring(stringStart, i));
                    }
                    ++i;  // skip both characters
                    c = oc;
                }
            }

            if (sb != null)
                sb.append(c);
        }

        if (sb != null)   return sb.toString();

        return s.substring(stringStart);
    }

    static char ESCAPE_C = '\\';
    // empty escape sequence to avoid a null name or illegal prefix
    static char NULL_ESCAPE_C = '=';
    static String NULL_ESCAPE = ESCAPE_C+""+NULL_ESCAPE_C;

    static String DANGEROUS_CHARS     = ".;:$[]<>/\\";
    static String REPLACEMENT_CHARS   = ",?!%{}^_|-";
    static char[] DANGEROUS_CHARS_A   = DANGEROUS_CHARS.toCharArray();
    static char[] REPLACEMENT_CHARS_A = REPLACEMENT_CHARS.toCharArray();

    static final long[] SPECIAL_BITMAP = new long[2];  // 128 bits
    static {
        String SPECIAL = DANGEROUS_CHARS + REPLACEMENT_CHARS + ESCAPE_C;
        //System.out.println("SPECIAL = "+SPECIAL);
        for (char c : SPECIAL.toCharArray()) {
            SPECIAL_BITMAP[c >>> 6] |= 1L << c;
        }
    }
    static boolean isSpecial(char c) {
        if ((c >>> 6) < SPECIAL_BITMAP.length)
            return ((SPECIAL_BITMAP[c >>> 6] >> c) & 1) != 0;
        else
            return false;
    }
    static char replacementOf(char c) {
        if (!isSpecial(c))  return c;
        int i = DANGEROUS_CHARS.indexOf(c);
        if (i < 0)  return c;
        return REPLACEMENT_CHARS.charAt(i);
    }
    static char originalOfReplacement(char c) {
        if (!isSpecial(c))  return c;
        int i = REPLACEMENT_CHARS.indexOf(c);
        if (i < 0)  return c;
        return DANGEROUS_CHARS.charAt(i);
    }
    static boolean isDangerous(char c) {
        if (!isSpecial(c))  return false;
        if (c == ESCAPE_C)  return false;  // not really dangerous
        return (DANGEROUS_CHARS.indexOf(c) >= 0);
    }
    static int indexOfDangerousChar(String s, int from) {
        for (int i = from, slen = s.length(); i < slen; i++) {
            if (isDangerous(s.charAt(i)))
                return i;
        }
        return -1;
    }

    // test driver
    public static void main(String[] av) {
        // If verbose is enabled, quietly check everything.
        // Otherwise, print the output for the user to check.
        boolean verbose = false;

        int maxlen = 0;

        while (av.length > 0 && av[0].startsWith("-")) {
            String flag = av[0].intern();
            av = java.util.Arrays.copyOfRange(av, 1, av.length); // Java 1.6 or later
            if (flag == "-" || flag == "--")  break;
            else if (flag == "-q")
                verbose = false;
            else if (flag == "-v")
                verbose = true;
            else if (flag.startsWith("-l"))
                maxlen = Integer.valueOf(flag.substring(2));
            else
                throw new Error("Illegal flag argument: "+flag);
        }

        if (maxlen == 0)
            maxlen = (verbose ? 2 : 4);
        if (verbose)  System.out.println("Note: maxlen = "+maxlen);

        switch (av.length) {
        case 0: av = new String[] {
                    DANGEROUS_CHARS.substring(0) +
                    REPLACEMENT_CHARS.substring(0, 1) +
                    NULL_ESCAPE + "x"
                }; // and fall through:
        case 1:
            char[] cv = av[0].toCharArray();
            av = new String[cv.length];
            int avp = 0;
            for (char c : cv) {
                String s = String.valueOf(c);
                if (c == 'x')  s = "foo";  // tradition...
                av[avp++] = s;
            }
        }
        if (verbose)
            System.out.println("Note: Verbose output mode enabled.  Use '-q' to suppress.");
        Tester t = new Tester();
        t.maxlen = maxlen;
        t.verbose = verbose;
        t.tokens = av;
        t.test("", 0);
    }

    static class Tester {
        boolean verbose;
        int maxlen;
        java.util.Map<String,String> map = new java.util.HashMap<String,String>();
        String[] tokens;

        void test(String stringSoFar, int tokensSoFar) {
            test(stringSoFar);
            if (tokensSoFar <= maxlen) {
                for (String token : tokens) {
                    if (token.length() == 0)  continue;  // skip empty tokens
                    if (stringSoFar.indexOf(token) != stringSoFar.lastIndexOf(token))
                        continue;   // there are already two occs. of this token
                    if (token.charAt(0) == ESCAPE_C && token.length() == 1 && maxlen < 4)
                        test(stringSoFar+token, tokensSoFar);  // want lots of \'s
                    else if (tokensSoFar < maxlen)
                        test(stringSoFar+token, tokensSoFar+1);
                }
            }
        }

        void test(String s) {
            // for small batches, do not test the null string
            if (s.length() == 0 && maxlen >=1 && maxlen <= 2)  return;
            String bn = testSourceName(s);
            if (bn == null)  return;
            if (bn == s) {
                //if (verbose)  System.out.println(s+" == id");
            } else {
                if (verbose)  System.out.println(s+" => "+bn+" "+toDisplayName(bn));
                String bnbn = testSourceName(bn);
                if (bnbn == null)  return;
                if (verbose)  System.out.println(bn+" => "+bnbn+" "+toDisplayName(bnbn));
                /*
                String bn3 = testSourceName(bnbn);
                if (bn3 == null)  return;
                if (verbose)  System.out.println(bnbn+" => "+bn3);
                */
            }
        }

        String testSourceName(String s) {
            if (map.containsKey(s))  return null;
            String bn = toBytecodeName(s);
            map.put(s, bn);
            String sn = toSourceName(bn);
            if (!sn.equals(s)) {
                String bad = (s+" => "+bn+" != "+sn);
                if (!verbose)  throw new Error("Bad mangling: "+bad);
                System.out.println("*** "+bad);
                return null;
            }
            return bn;
        }
    }
}
