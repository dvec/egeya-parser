import org.jsoup.nodes.Element

class HTMLParser {
    Element body

    HTMLParser(Element body) {
        this.body = body
    }

    private static boolean isEquals(String s1, String s2) {
        if (s1.length() > s2.length()) return false
        while (s1.contains("*")) {
            def index = s1.indexOf("*")
            def indexFromEnd =  s1.length() - index

            def s1rev = s1.reverse()[0..<indexFromEnd - 1]
            def s2rev = s2.reverse()[0..<indexFromEnd - 1]

            s1 = s1[0..<index - 1]
            s2 = s2[0..<index - 1]

            if (s1 != s2 || s1rev != s2rev) return false
        }
        return s1 == s2
    }

    private static boolean isArgumentEquals(Element e, Map<String, String> arg) {
        for (a in arg.keySet()) {
            if (!isEquals(arg[a], e.attr(a))) return false
        }
        return true
    }

    private static boolean isSuitable(Element e, String name, Map<String, String> arg) {
        return isEquals(name, e.tagName()) && isArgumentEquals(e, arg)
    }

    private static Tuple performBraces(String message) {
        if (message.count('[') == 1 && message.count(']') == 1) {
            def arg = [ : ]
            def arguments = message[message.indexOf('[') + 1..message.indexOf(']') - 1]
            for (String s: arguments.split(" ")) {
                def parts = s.split("=")
                arg[parts[0]] = parts[1].replace("'", "")
            }
            return new Tuple(message[0..message.indexOf('[') - 1].trim(), arg)
        } else {
            return new Tuple(message, [ : ])
        }
    }


    private static Tuple performParam(String message) {
        if (message.count('@') == 1) {
            def parts = message.split('@')
            return new Tuple(parts[0], parts[1])
        } else {
            return new Tuple(message, null)
        }
    }

    private def perform(List<String> p, Element e) {
        def (message, arg) = performBraces(p[0])
        def (name, param) = performParam(message)

        ArrayList<String> toReturn = new ArrayList<>()
        for (c in e.children()) {
            if (!isSuitable(c, name, arg)) continue
            if (p.size() > 1) {
                toReturn += perform(p[1..-1], c)
            } else {
                if (param != null) {
                    toReturn << c.attr(param).trim()
                } else {
                    toReturn << c.text().trim()
                }
            }
        }

        if (toReturn.size() > 1) {
            return toReturn
        } else if (toReturn.size() == 1) {
            return toReturn[0]
        }
        return null
    }

    ArrayList run(String command) {
        return perform(command.split("/").toList(), body) ?: []
    }
}
