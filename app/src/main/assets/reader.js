(function() {
    try {
        var doc = document.cloneNode(true);

        // Remove junk elements
        var selectorsToRemove = [
            'script', 'style', 'noscript', 'iframe', 'header', 'footer', 'nav',
            'aside', '.nav', '.navbar', '.sidebar', '.menu', '.header', '.footer',
            '.ad', '.ads', '.advertisement', '.social-share', '.share-buttons',
            '.comments', '#comments', '.cookie-banner', '.newsletter', '.popup',
            '[aria-hidden="true"]', '[role="navigation"]', '[role="banner"]', '[role="complementary"]'
        ];
        selectorsToRemove.forEach(function(sel) {
            var elements = doc.querySelectorAll(sel);
            for (var i = 0; i < elements.length; i++) {
                elements[i].parentNode && elements[i].parentNode.removeChild(elements[i]);
            }
        });

        // Determine title
        var title = "";
        var ogTitle = doc.querySelector('meta[property="og:title"]');
        var h1 = doc.querySelector('h1');
        if (ogTitle && ogTitle.content) {
            title = ogTitle.content.trim();
        } else if (h1 && h1.textContent.trim()) {
            title = h1.textContent.trim();
        } else {
            title = doc.title ? doc.title.trim() : "";
        }

        // Determine byline
        var byline = "";
        var authorMeta = doc.querySelector('meta[name="author"]') || doc.querySelector('meta[property="article:author"]');
        var authorElem = doc.querySelector('.byline, .author, [rel="author"], .post-author');
        if (authorMeta && authorMeta.content) {
            byline = authorMeta.content.trim();
        } else if (authorElem) {
            byline = authorElem.textContent.trim();
        }

        // Determine lead image
        var leadImage = "";
        var ogImage = doc.querySelector('meta[property="og:image"]');
        if (ogImage && ogImage.content) {
            leadImage = ogImage.content;
        }

        // Find primary article container
        var articleCandidate = doc.querySelector('article, [itemprop="articleBody"], main, [role="main"], .post-content, .entry-content, .article-body, #content');
        if (!articleCandidate) {
            articleCandidate = doc.body;
        }

        // Extract paragraphs and headings
        var contentNodes = articleCandidate.querySelectorAll('p, h2, h3, h4, blockquote, ul, ol, pre');
        var cleanHtml = "";
        var totalWords = 0;

        for (var j = 0; j < contentNodes.length; j++) {
            var node = contentNodes[j];
            var text = node.textContent.trim();
            if (text.length > 20 || node.tagName.toLowerCase().startsWith('h')) {
                var words = text.split(/\s+/).filter(function(w) { return w.length > 0; }).length;
                totalWords += words;

                var tag = node.tagName.toLowerCase();
                var safeContent = node.innerHTML
                    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, "")
                    .replace(/on\w+="[^"]*"/g, "");
                cleanHtml += "<" + tag + ">" + safeContent + "</" + tag + ">";
            }
        }

        if (!cleanHtml && articleCandidate) {
            cleanHtml = articleCandidate.innerHTML
                .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, "")
                .replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, "");
            totalWords = cleanHtml.replace(/<[^>]*>/g, " ").split(/\s+/).length;
        }

        var readingMinutes = Math.max(1, Math.round(totalWords / 200));

        var result = {
            title: title || "Untitled Article",
            byline: byline,
            leadImage: leadImage,
            wordCount: totalWords,
            readingTimeMinutes: readingMinutes,
            contentHtml: cleanHtml,
            url: window.location.href,
            siteName: window.location.hostname
        };

        return JSON.stringify(result);
    } catch(e) {
        return JSON.stringify({
            title: document.title || "Article",
            byline: "",
            leadImage: "",
            wordCount: 0,
            readingTimeMinutes: 1,
            contentHtml: "<p>Unable to parse article in Reader View.</p>",
            url: window.location.href,
            siteName: window.location.hostname
        });
    }
})();
