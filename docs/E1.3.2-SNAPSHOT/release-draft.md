Elda 1.3.2 Release Notes Draft
==============================

This release of Elda is mosty bug-fix and tidy-up.

Elda now assumes the SPARQL endpoint supports SPARQL 1.1
and instead of using nested selects to reduce view
query size uses VALUES, which is simpler to generate
and produces smaller and less complicated queries.

The artifact elda-bundled, which was a single WAR
containing bith elda-common and elda-assets, has been
removed. (Users can construct their own bundle by
unpacking comman and assets, merging, and then making
a new warfile.)

The artifact elda-system-tests has been removed
because it no longer did anything. (Instead, 
elda-testing-webapp now performs integratration tests.
Users do not need to use this artifact; it is part of
the Elda development process.)

The error-page mechanism introduced in earlier 1.3.*
Eldas has been revised as it was unsatisfactory.
Instead, error pages are rendered from velocity macros
with appropriate names. The pages are found by searching

	* the user-specified _velocityPath
	* /etc/elda/conf.d/{APP}/error-pages/velocity/
	* the webapps webapp/error_pages/velocity/

This allows elda-common to contain a default set
of error-page macros, which can be over-ridden using
pages inside /etc or configured in the LDA config file.

The supplied default pages inspect the value of the API
variable _errorMode. If this is present and "taciturn", 
then they do not show detail information (eg a reason why a
request might be bad). Otherwise they show whatever
detail is available.

Obsolete .xsl files have been removed.

The URI template definitions which include query
parameters like

	path/element?name={spoo}

have been generalised so that the value of the query parameter
can contain multiple {}-enclosed variables and literal
characters such as

	path/element?where={x},{y}

A bug in the handling of default languages with _lang which
caused Elda to generate inappropriate SPARQL with unbound
variables in it has been fixed.



