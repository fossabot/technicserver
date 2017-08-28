---
title: Contributors
---
{% for c in site.github.contributors %}
[![avatar]({{ c.avatar_url }}){: height="36px" width="36px"} {{ c.login }}]({{ c.html_url }}) ({{ c.contributions }} contributions)
{% endfor %}
