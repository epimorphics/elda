// extract the h1 and h2 elements from the text and generate
// a table of contents attached to the node with ID="toc".

var count = 1000

function recurse(d, b) {
	var children = b.childNodes
	for (var i = 0; i < children.length; i += 1) {
		var node = children[i]
		var tag = node.nodeName.toLowerCase()
		var show = !/not-toc/.test(node.className)
		if (show && (tag == "h1" || tag == "h2")) {
			var x = document.createElement("div")
			if (tag == "h1") x.style = "margin-top: 1ex"
			var a = document.createElement("a")
			x.className = "content-" + tag
			a.innerHTML = node.innerHTML
			count += 1
			a.style = "text-decoration: none"
			a.href = "#section-" + count
			x.appendChild(a)
			d.appendChild( x )
			node.id = "section-" + count
		} else {
			recurse(d, node)
		}	
	} 
}

function toc() {
	var d = document.getElementById("toc")
	var b = document.body
	recurse(d, b)
}
