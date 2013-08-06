package main

import "fmt"
import "strconv"
import "strings"
import "math/rand"

type node interface {
	String() string
	isResource() bool
}

type triple struct{ S, P, O node }

type graph map[triple]struct{}

func (g graph) add(S, P, O node) graph {
	g[triple{S, P, O}] = struct{}{}
	return g
}

func main() {
	fmt.Println("# fabricating RDF data for Elda regressions.")
	g := graph{}
	fillGraph(g)
	writeGraph(g)
}

type property struct {
	p resource
	v []node
	t resource
}

var prefixFor = map[string]string{
	"rdfs": "http://www.w3.org/2000/01/rdf-schema#",
	"x":    "http://example.org/rdf/prefixes/x#",
	"some": "http://example.org/rdf/prefixes/some#",
	"xsd":  "http://www.w3.org/2001/XMLSchema#",
}

func q(s string) resource {
	i := strings.Index(s, ":")
	return resource(prefixFor[s[:i]] + s[i+1:])
}

type typed struct {
	l     string
	ltype resource
}

func (t typed) String() string {
	return `"` + t.l + `"` + "^^" + t.ltype.String()
}

func (t typed) isResource() bool {
	return false
}

type lint int

func (l lint) String() string {
	return strconv.Itoa(int(l))
}

func (l lint) isResource() bool {
	return false
}

var topProperties = []property{
	{
		q("x:p1"),
		[]node{lint(0), lint(1), lint(2), lint(10), lint(100), lint(1000)},
		q("xsd:integer"),
	},
	{
		q("x:p2"),
		[]node{
			q("x:Alpha"),
			q("x:Bravo"),
			q("x:Charlie"),
			q("x:Delta"),
			q("x:Echo"),
			q("x:Foxtrot"),
			q("x:Golf"),
			q("x:Hotel"),
			q("x:India"),
		},
		q("some:resourceType"),
	},
	{
		q("x:p3"),
		[]node{
			q("x:Aluminium"),
			q("x:Boron"),
			q("x:Carbon"),
			q("x:Dubnium"),
			q("x:Erbium"),
			q("x:Fluorine"),
			q("x:Gold"),
			q("x:Helium"),
		},
		q("rdfs:Resource")},
	{
		q("x:p4"),
		[]node{
			typed{"FEED", q("some:code")},
			typed{"FACE", q("some:code")},
			typed{"FADE", q("some:code")},
			typed{"CAFE", q("some:code")},
			typed{"DAFE", q("some:code")},
			typed{"BEAD", q("some:code")},
		},
		q("rdfs:Resource")},
}

var nextProperties = []property{
	{
		q("x:northing"),
		[]node{decimal(0), decimal(17), decimal(144), decimal(280), decimal(10000)},
		q("xsd:decimal"),
	},
	{
		q("x:easting"),
		[]node{decimal(98), decimal(415), decimal(1066), decimal(12345), decimal(1001)},
		q("some:type"),
	},
	{
		q("x:animal"),
		[]node{
			q("x:Aardvark"),
			q("x:Bison"),
			q("x:Cat"),
			q("x:Dingo"),
			q("x:Elephant"),
			q("x:Ferret"),
			q("x:Gazelle"),
			q("x:Hawk"),
		},
		q("rdfs:Resource"),
	},
}

var lastProperties = []property{
	{q("x:p1"), []node{lint(0), lint(1), lint(10), lint(100), lint(1000)}, q("xsd:integer")},
	{q("x:p2"), []node{q("x:A"), q("x:B")}, q("some:type")},
	{q("x:p3"), []node{q("x:C")}, q("rdfs:Resource")},
	{q("x:p4"), []node{typed{"lexicalForm", q("some:type")}}, q("rdfs:Resource")},
}

var propertySets = [][]property{
	topProperties,
	nextProperties,
	lastProperties,
	[]property{},
}

func fillGraph(g graph) {
	const subjects = 100
	const RDFS_label = resource("http://www.w3.org/2000/01/rdf-schema#" + "label")
	const RDF_type = resource("http://www.w3.org/1999/02/22-rdf-syntax-ns#" + "type")

	for si := 0; si < subjects; si += 1 {
		S := resource("http://example.com/rdf/resource/" + strconv.Itoa(si))
		g.add(S, RDFS_label, literal(wordify(si)))
		g.add(S, RDF_type, q("x:Item"))
		nesting(g, S, 0)
	}
}

func nesting(g graph, r resource, depth int) {
	fmt.Println("#", "resource", r, "depth", depth, "properties", len(propertySets[depth]))
	const ODDS_NESTING = 10
	for _, property := range propertySets[depth] {
		v := property.v
		i := len(v)
		val := v[rand.Intn(i)]
		g.add(r, property.p, val)
		if val.isResource() && rand.Intn(ODDS_NESTING) == 1 {
			nesting(g, val.(resource), depth+1)
		}
	}
}

type decimal int

func (d decimal) String() string {
	return fmt.Sprintf("%v", float64(d)/100)
}

func (d decimal) isResource() bool {
	return false
}

type resource string

func (r resource) String() string {
	return "<" + string(r) + ">"
}

func (r resource) isResource() bool {
	return true
}

type literal string

func (l literal) String() string {
	return `"` + string(l) + `"`
}

func (l literal) isResource() bool {
	return false
}

func writeGraph(g graph) {
	for t := range g {
		writeTriple(t)
	}
}

func writeTriple(t triple) {
	fmt.Println(t.S.String(), " ", t.P.String(), " ", t.O.String(), " .")
}

func wordify(i int) string {
	return consonantly(i)
}

var consonant = [...]string{
	"big", "cold", "dark", "fun",
	"great", "jammy", "ked", "light",
	"magic", "null", "proud", "quick",
	"stark", "tan", "vile", "zoo",
	"hollow",
}

var lastConsonant = [...]string{
	"brick", "carpet", "duck", "fin",
	"gate", "joint", "key", "leaf",
	"mat", "nut", "pan", "crown",
	"salt", "shirt", "tea", "vine",
	"zebra",
}

func consonantly(i int) string {
	if i == 0 {
		return ""
	}
	v := i / 17
	if v == 0 {
		return lastConsonant[i%17]
	}
	return consonant[i%17] + "-" + consonantly(v)
}
