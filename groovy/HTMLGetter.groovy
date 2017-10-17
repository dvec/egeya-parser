import org.jsoup.Jsoup

class HTMLGetter {
    class Post {
        String header
        String date
        String img
        String url
    }

    class Version {
        String header
        String date
        String img
        String url
    }

    private final versions = [
            new Version(
                    header: "main/div [class='container']/article/header/h2/a",
                    date: "main/div/article/header/p/time@datetime",
                    img: "main/div/article/section/figure/a/img@src",
                    url: "main/div/article/header/h2/a@href"),
            new Version(
                    header: "div [class='common']/div [class='content']/div [id='e2-note-*']/article/h1/a/span",
                    date: "div [class='common']/div [class='content']/div [id='e2-note-*']/div [class='e2-note-tags]/span@title",
                    img: "div [class='common']/div [class='content']/div [id='e2-note-*']/article/div/div/div/div/img@src",
                    url: "div [class='common']/div [class='content']/div [id='e2-note-*']/article/h1/a@href")

    ]

    private String url
    private HTMLParser parser
    private Integer version

    HTMLGetter(String url) {
        this.url = url
    }

    private static boolean isExternalUrl(String url) {
        return url.charAt(0) != '/'
    }

    private String getHostUrl() {
        URL uri = new URL(url)
        return uri.getProtocol() + "://" + uri.getHost()
    }

    void loadHTML() {
        def body = Jsoup.connect(url).get().body()
        this.parser = new HTMLParser(body)
    }

    ArrayList<Post> perform() {
        int from = 0
        if (version != null) from = version
        for (int i = from; i < versions.size(); i++) {
            def header = parser.run(versions[i].header)
            def date = parser.run(versions[i].date)
            def img = parser.run(versions[i].img)
            def url = parser.run(versions[i].url)

            for (int j = 0; j < img.size(); j++) {
                if (img[j] != null && !isExternalUrl(img[j])) {
                    img[j] = getHostUrl() + img[j]
                }
                if (url[j] != null && !isExternalUrl(url[j])) {
                    url[j] = getHostUrl() + url[j]
                }
            }


            def posts = []
            if (header != [] || date != [] || img != [] || url != []) {
                for (int j = 0; j < header.size(); j++) {
                    posts << new Post(header: header[j], date: date[j], img: img[j], url: url[j])
                }
                version = i
                return posts
            }

            if (version != null) return null
        }
        return null
    }
}