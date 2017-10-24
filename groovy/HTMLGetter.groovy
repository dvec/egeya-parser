import groovy.sql.Sql
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class HTMLGetter {
    class Post {
        String header
        String date
        String img
        String url

        String getUUID() {
            return url + "_" + date
        }
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
    private int version = -1
    private int page = 1

    HTMLGetter(String url) {
        this.url = url
    }

    private String getHostUrl() {
        URL uri = new URL(url)
        return uri.getProtocol() + "://" + uri.getHost()
    }

    private static boolean isExternalUrl(String url) {
        return url.charAt(0) != '/'
    }

    private ArrayList<Post> nextPage() {
        String url = this.url
        if (page != 1) url += "page${page}"
        page++

        Element body
        try {
            body = Jsoup.connect(url).get().body()
        } catch (HttpStatusException ignored) {
            return null
        }

        HTMLParser parser = new HTMLParser(body)
        int from = 0
        if (version != -1) from = version
        for (int i = from; i < versions.size(); i++) {
            ArrayList header = parser.run(versions[i].header)
            ArrayList date = parser.run(versions[i].date)
            ArrayList img = parser.run(versions[i].img)
            ArrayList postUrl = parser.run(versions[i].url)

            if (img.size() == 0) return null
            for (int j = 0; j < img.size(); j++) {
                if (img[j] != null && !isExternalUrl(img[j])) {
                    img[j] = getHostUrl() + img[j]
                }
                if (postUrl[j] != null && !isExternalUrl(postUrl[j])) {
                    postUrl[j] = getHostUrl() + postUrl[j]
                }
            }


            ArrayList posts = []
            if (header != [] || date != [] || img != [] || postUrl != []) {
                for (int j = 0; j < header.size(); j++) {
                    if (postUrl[j] != null) {
                        posts << new Post(header: header[j], date: date[j], img: img[j], url: postUrl[j])
                    }
                }
                version = i
                return posts
            }

            if (version != null) return null
        }
        return null
    }

    ArrayList<Post> getNewPosts(Sql sql, String table) {
        ArrayList page
        ArrayList<Post> posts = new ArrayList()
        while (true) {
            page = nextPage()
            if (page == null) break
            int i = 0
            for (Post post: page) {
                i++
                if ((int) sql.rows(String.format("SELECT COUNT(*) FROM %s WHERE uuid = '%s'", table, post.getUUID()))
                        .first().get("count") == 0) {
                    posts.add(post)
                } else break
            }
        }
        return posts
    }
}