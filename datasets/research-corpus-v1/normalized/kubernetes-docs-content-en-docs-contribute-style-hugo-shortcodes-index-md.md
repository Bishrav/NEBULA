This page explains the custom Hugo shortcodes that can be used in Kubernetes Markdown documentation.

Read more about shortcodes in the [Hugo documentation](https://gohugo.io/content-management/shortcodes).

## Feature state

In a Markdown page (`.md` file) on this site, you can add a shortcode to
display version and state of the documented feature.

### Feature state demo

Below is a demo of the feature state snippet, which displays the feature as
stable in the latest Kubernetes version.

```
```

Renders to:

The valid values for `state` are:

* alpha
* beta
* deprecated
* stable

### Feature state code

The displayed Kubernetes version defaults to that of the page or the site. You can change the
feature state version by passing the `for_k8s_version` shortcode parameter. For example:

```
```

Renders to:

### Feature state retrieval from description file

To dynamically determine the state of the feature, make use of the `feature_gate_name`
shortcode parameter. The feature state details will be extracted from the corresponding feature gate
description file located in `content/en/docs/reference/command-line-tools-reference/feature-gates/`.
For example:

```
```

Renders to:

## Feature gate description

In a Markdown page (`.md` file) on this site, you can add a shortcode to
display the description for a shortcode.

### Feature gate description demo

Below is a demo of the feature state snippet, which displays the feature as
stable in the latest Kubernetes version.

```
```

Renders to:

## Glossary

There are two glossary shortcodes: `glossary_tooltip` and `glossary_definition`.

You can reference glossary terms with an inclusion that automatically updates
and replaces content with the relevant links from [our glossary](/docs/reference/glossary/).
When the glossary term is moused-over, the glossary entry displays a tooltip.
The glossary term also displays as a link.

As well as inclusions with tooltips, you can reuse the definitions from the glossary in
page content.

The raw data for glossary terms is stored at
[the glossary directory](https://github.com/kubernetes/website/tree/main/content/en/docs/reference/glossary),
with a content file for each glossary term.

### Glossary demo

For example, the following include within the Markdown renders to

```
```

Here's a short glossary definition:

```
```

which renders as:

You can also include a full definition:

```
```

which renders as:

## Links to API Reference

You can link to a page of the Kubernetes API reference using the
`api-reference` shortcode, for example to the

```
```

The content of the `page` parameter is the suffix of the URL of the API reference page.

You can link to a specific place into a page by specifying an `anchor`
parameter, for example to the {{< api-reference page="core/pod-v1" anchor="PodSpec" >}}
reference or the {{< api-reference page="core/pod-v1" anchor="environment-variables" >}}
section of the page:

```
```

You can change the text of the link by specifying a `text` parameter, for
example by linking to the
section of the page:

```
```

## Table captions

You can make tables more accessible to screen readers by adding a table caption. To add a
[caption](https://www.w3schools.com/tags/tag_caption.asp) to a table,
enclose the table with a `table` shortcode and specify the caption with the `caption` parameter.

Table captions are visible to screen readers but invisible when viewed in standard HTML.

Here's an example:

```go-html-template
Parameter | Description | Default
:---------|:------------|:-------
`timeout` | The timeout for requests | `30s`
`logLevel` | The log level for log output | `INFO`
```

The rendered table looks like this:

Parameter | Description | Default
:---------|:------------|:-------
`timeout` | The timeout for requests | `30s`
`logLevel` | The log level for log output | `INFO`

If you inspect the HTML for the table, you should see this element immediately
after the opening `<table>` element:

```html
<caption style="display: none;">Configuration parameters</caption>
```

## Tabs

In a markdown page (`.md` file) on this site, you can add a tab set to display
multiple flavors of a given solution.

The `tabs` shortcode takes these parameters:

* `name`: The name as shown on the tab.
* `codelang`: If you provide inner content to the `tab` shortcode, you can tell Hugo
  what code language to use for highlighting.
* `include`: The file to include in the tab. If the tab lives in a Hugo
  [leaf bundle](https://gohugo.io/content-management/page-bundles/#leaf-bundles),
  the file -- which can be any MIME type supported by Hugo -- is looked up in the bundle itself.
  If not, the content page that needs to be included is looked up relative to the current page.
  Note that with the `include`, you do not have any shortcode inner content and must use the
  self-closing syntax. For example,
  `{{</* tab name="Content File #1" include="example1" /*/>}}`. The language needs to be specified
  under `codelang` or the language is taken based on the file name.
  Non-content files are code-highlighted by default.
* If your inner content is markdown, you must use the `%`-delimiter to surround the tab.
  For example, `{{%/* tab name="Tab 1" %}}This is **markdown**{{% /tab */%}}`
* You can combine the variations mentioned above inside a tab set.

Below is a demo of the tabs shortcode.

The tab **name** in a `tabs` definition must be unique within a content page.

### Tabs demo: Code highlighting

```go-text-template
echo "This is tab 1."
println "This is tab 2."
```

Renders to:

echo "This is tab A."
// If you don't set a tab name, the site supplies one.
println "This tab has a default name";

### Tabs demo: Inline Markdown and HTML

```go-html-template
This is **some markdown.**
It can even contain shortcodes.
<div>
	<h3>Plain HTML</h3>
	<p>This is some <i>plain</i> HTML.</p>
</div>
```

Renders to:

This is **some markdown.**

It can even contain shortcodes.

<div>
	<h3>Plain HTML</h3>
	<p>This is some <i>plain</i> HTML.</p>
</div>

### Tabs demo: File include

```go-text-template
```

Renders to:

## Source code files

You can use the `{{%/* code_sample */%}}` shortcode to embed the contents of file in a code block to allow users to download or copy its content to their clipboard. This shortcode is used when the contents of the sample file is generic and reusable, and you want the users to try it out themselves.

This shortcode takes in two named parameters: `language` and `file`. The mandatory parameter `file` is used to specify the path to the file being displayed. The optional parameter `language` is used to specify the programming language of the file. If the `language` parameter is not provided, the shortcode will attempt to guess the language based on the file extension.

For example:

```none
```

The output is:

When adding a new sample file, such as a YAML file, create the file in one of the `<LANG>/examples/` subdirectories where `<LANG>` is the language for the page. In the markdown of your page, use the `code` shortcode:

```none
```
where `<RELATIVE-PATH>` is the path to the sample file to include, relative to the `examples` directory. The following shortcode references a YAML file located at `/content/en/examples/configmap/configmaps.yaml`.

```none
```

The legacy `{{%/* codenew */%}}` shortcode is being replaced by `{{%/* code_sample */%}}`.
Use `{{%/* code_sample */%}}` (not `{{%/* codenew */%}}` or `{{%/* code */%}}`) in new documentation.

## Third party content marker

Running Kubernetes requires third-party software. For example: you
usually need to add a
[DNS server](/docs/tasks/administer-cluster/dns-custom-nameservers/#introduction)
to your cluster so that name resolution works.

When we link to third-party software, or otherwise mention it,
we follow the [content guide](/docs/contribute/style/content-guide/)
and we also mark those third party items.

Using these shortcodes adds a disclaimer to any documentation page
that uses them.

### Lists {#third-party-content-list}

For a list of several third-party items, add:
```
```
just below the heading for the section that includes all items.

### Items {#third-party-content-item}

If you have a list where most of the items refer to in-project
software (for example: Kubernetes itself, and the separate
[Descheduler](https://github.com/kubernetes-sigs/descheduler)
component), then there is a different form to use.

Add the shortcode:
```
```

before the item, or just below the heading for the specific item.

## Details

You can render a `<details>` HTML element using a shortcode:

```markdown
The frobnicator extension API implements _widgets_ using example running text.

Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur,
adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et
dolore magnam aliquam quaerat voluptatem.
```

This renders as:
The frobnicator extension API implements _widgets_ using example running text.

Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur,
adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et
dolore magnam aliquam quaerat voluptatem.

Use this shortcode sparingly; it is usually best to have all of the text directly shown
to readers.

## Version strings

To generate a version string for inclusion in the documentation, you can choose from
several version shortcodes. Each version shortcode displays a version string derived from
the value of a version parameter found in the site configuration file, `hugo.toml`.
The two most commonly used version parameters are `latest` and `version`.

### `{{</* param "version" */>}}`

The `{{</* param "version" */>}}` shortcode generates the value of the current
version of the Kubernetes documentation from the `version` site parameter. The
`param` shortcode accepts the name of one site parameter, in this case:
`version`.

In previously released documentation, `latest` and `version` parameter values
are not equivalent.  After a new version is released, `latest` is incremented
and the value of `version` for the documentation set remains unchanged. For
example, a previously released version of the documentation displays `version`
as `v1.19` and `latest` as `v1.20`.

Renders to:

### `{{</* latest-version */>}}`

The `{{</* latest-version */>}}` shortcode returns the value of the `latest` site parameter.
The `latest` site parameter is updated when a new version of the documentation is released.
This parameter does not always match the value of `version` in a documentation set.

Renders to:

### `{{</* latest-semver */>}}`

The `{{</* latest-semver */>}}` shortcode generates the value of `latest`
without the "v" prefix.

Renders to:

### `{{</* version-check */>}}`

The `{{</* version-check */>}}` shortcode checks if the `min-kubernetes-server-version`
page parameter is present and then uses this value to compare to `version`.

Renders to:

### `{{</* latest-release-notes */>}}`

The `{{</* latest-release-notes */>}}` shortcode generates a version string
from `latest` and removes the "v" prefix. The shortcode prints a new URL for
the release note CHANGELOG page with the modified version string.

Renders to:

## {{% heading "whatsnext" %}}

* Learn about [Hugo](https://gohugo.io/).
* Learn about [writing a new topic](/docs/contribute/style/write-new-topic/).
* Learn about [page content types](/docs/contribute/style/page-content-types/).
* Learn about [opening a pull request](/docs/contribute/new-content/open-a-pr/).
* Learn about [advanced contributing](/docs/contribute/advanced/).
