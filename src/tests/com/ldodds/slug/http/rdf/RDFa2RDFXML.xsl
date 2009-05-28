<?xml version="1.0" encoding="UTF-8"?>
<!--
	===============================================================================
	RDFa2RDFXML.xsl
	This is a GRDDL transformation from XHTML+RDFa to RDF/XML.
	
	It is based on the algorithm in the Candidate Recommendation dated 
	20 June 2008 at
	http://www.w3.org/TR/2008/CR-rdfa-syntax-20080620
	
	Copyright TSO 2008
	===============================================================================
	
	Creation date: 23/01/2008
	Created by:    Jeni Tennison
	Source DTD:    schemaGazette-v1-0.xsd
	Result schema: gazette.dtd (XHTML+RDFa)
	XSLT version:  1.0
	XSLT engine:   any
	White space:   preserve
	
	===============================================================================
	Version history
	
	Version no. Name            Date           Reason
	_______________________________________________________________________________
	
	0.1         Jeni Tennison   09/02/2008 
	0.2         Jeni Tennison   16/06/2008     Better handling of lack of base URI
	                                           Ignoring areas of document with no RDFa (for speed)
	                                           Added support for typeof as synonym for instanceof
	0.3         Jeni Tennison   15/07/2008     Fixed incomplete triple support
	                                           Fixed URI resolution problems
	0.4         Jeni Tennison   16/07/2008     Changed output encoding to US-ASCII
	                                           Added support for xml:base (TC 4)
	                                           Fixed support for XMLLiteral (TC 11)
	                                           Added $debug parameter
	                                           Modified to use new (CR) algorithm (TC 33)
	                                           Fixed URIs for HTML link types (TC 40)
	                                           Fixed handling of multiple properties (TC 54)
	                                           Fixed handling of CURIE with no prefix (TC 63)
	                                           Added some missed HTML link types (TC 76)
  0.5         Jeni Tennison   18/07/2008     Removed whitespace normalization of literals (TC 99)
                                             Ensured preservation of xml:lang within XML literals (TC 101)
                                             Testing for presence of attributes rather than parsed value (TC 105)
                                             Removed support for xml:base (TC 109) *Sigh*
-->
<xsl:stylesheet  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:h="http://www.w3.org/1999/xhtml"
  xmlns:hv="http://www.w3.org/1999/xhtml/vocab#"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:exsl="http://exslt.org/common"
  xmlns:msxsl="urn:schemas-microsoft-com:xslt"  
  exclude-result-prefixes="h msxsl exsl">

<xsl:param name="sourceURL" />

<xsl:param name="debug" select="false()" />

<xsl:output method="xml" media-type="application/rdf+xml" 
	indent="yes" encoding="US-ASCII" />

<xsl:variable name="g_strHTMLvocab"
  select="'http://www.w3.org/1999/xhtml/vocab#'" />
<xsl:variable name="g_strHTMLlinkTypes"
  select="' alternate appendix bookmark cite chapter contents copyright first glossary help icon index last license meta next p3pv1 prev role section stylesheet subsection start top up '" />
<xsl:variable name="g_strRDFtype" 
	select="'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'" />
<xsl:variable name="g_strRDFxmlLiteral" 
	select="'http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral'" />

<xsl:variable name="g_strDefaultBNode"
  select="concat('o', generate-id(.))" />

<xsl:template match="/">
  <xsl:variable name="rtfRDFXML">
		<xsl:apply-templates select="." mode="RDFa2RDFXML" />
  </xsl:variable>
  <xsl:variable name="nstRDFXML" select="exsl:node-set($rtfRDFXML)" />
	<xsl:apply-templates select="$nstRDFXML/rdf:RDF" mode="normalizeRDFXML">
		<xsl:with-param name="nstURImappings" select="$nstRDFXML/*/namespace::*" />
	</xsl:apply-templates>
</xsl:template>

<!-- *** Normalizing RDF/XML *** -->

<xsl:key name="keyStatementsBySubject" 
	match="rdf:Statement" 
	use="rdf:subject/@rdf:resource | rdf:subject/@rdf:nodeID" />

<xsl:template match="rdf:RDF" mode="normalizeRDFXML">
	<xsl:param name="nstURImappings" select="/.." />
	<rdf:RDF>
		<xsl:copy-of select="$nstURImappings" />
		<xsl:for-each select="rdf:Statement[generate-id() = 
			generate-id(key('keyStatementsBySubject', rdf:subject/@rdf:resource | rdf:subject/@rdf:nodeID)[1])]">
			<xsl:variable name="nstSubject"
				select="rdf:subject/@rdf:resource | rdf:subject/@rdf:nodeID" />
			<xsl:variable name="nstStatements"
				select="key('keyStatementsBySubject', $nstSubject)" />
			<xsl:variable name="nstTypes"
				select="$nstStatements[rdf:predicate/@rdf:resource = $g_strRDFtype]" />
			<xsl:variable name="rtfPredicates">
				<xsl:apply-templates select="$nstTypes[position() > 1] |
					                           $nstStatements[rdf:predicate/@rdf:resource != $g_strRDFtype]"
					mode="normalizeRDFXML">
					<xsl:with-param name="nstURImappings" select="$nstURImappings" />
				</xsl:apply-templates>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="$nstTypes">
					<xsl:variable name="strType"
						select="$nstTypes[1]/rdf:object/@rdf:resource" />
					<xsl:variable name="strLocalName">
						<xsl:call-template name="getLastPart">
							<xsl:with-param name="strURI" select="$strType" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="strNamespace"
						select="substring-before($strType, $strLocalName)" />
					<xsl:variable name="strPrefix"
						select="name($nstURImappings[. = $strNamespace])" />
					<xsl:element name="{$strPrefix}:{$strLocalName}" namespace="{$strNamespace}">
						<xsl:apply-templates select="$nstSubject" mode="normalizeRDFXML" />
						<xsl:copy-of select="$rtfPredicates" />
					</xsl:element>
				</xsl:when>
				<xsl:otherwise>
					<rdf:Description>
						<xsl:apply-templates select="$nstSubject" mode="normalizeRDFXML" />
						<xsl:copy-of select="$rtfPredicates" />
					</rdf:Description>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</rdf:RDF>
</xsl:template>

<xsl:template match="rdf:Statement" mode="normalizeRDFXML">
	<xsl:param name="nstURImappings" select="/.."/>
	<xsl:variable name="strPredicate"
		select="rdf:predicate/@rdf:resource" />
	<xsl:variable name="strLocalName">
		<xsl:call-template name="getLastPart">
			<xsl:with-param name="strURI" select="$strPredicate" />
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="strNamespace"
		select="substring-before($strPredicate, $strLocalName)" />
	<xsl:variable name="strPrefix"
		select="name($nstURImappings[. = $strNamespace])" />
	<xsl:element name="{$strPrefix}:{$strLocalName}" namespace="{$strNamespace}">
		<xsl:copy-of select="rdf:object/@*" />
		<xsl:copy-of select="rdf:object/node()" />
	</xsl:element>
</xsl:template>

<xsl:template match="rdf:subject/@rdf:resource" mode="normalizeRDFXML">
	<xsl:attribute name="rdf:about">
		<xsl:value-of select="." />
	</xsl:attribute>
</xsl:template>

<xsl:template match="rdf:subject/@rdf:nodeID" mode="normalizeRDFXML">
	<xsl:copy-of select="." />
</xsl:template>

<xsl:template name="getLastPart">
	<xsl:param name="strURI" />
	<xsl:choose>
		<xsl:when test="contains($strURI, '#')">
			<xsl:value-of select="substring-after($strURI, '#')" />
		</xsl:when>
		<xsl:when test="contains($strURI, '/')">
			<xsl:call-template name="getLastPart">
				<xsl:with-param name="strURI" select="substring-after($strURI, '/')" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$strURI" />
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!-- *** Generating RDF/XML from RDFa *** -->

<xsl:template match="/" mode="RDFa2RDFXML">
  <xsl:variable name="strBase">
    <xsl:choose>
      <xsl:when test="h:html/h:head/h:base">
        <xsl:value-of select="h:html/h:head/h:base/@href" />
      </xsl:when>
      <xsl:when test="$sourceURL != ''">
        <xsl:value-of select="$sourceURL" />
      </xsl:when>
      <!--
      <xsl:when test="h:html/@xml:base">
        <xsl:message>
          <xsl:text>WARNING: xml:base isn't allowed in XHTML+RDFa</xsl:text>
        </xsl:message>
        <xsl:value-of select="h:html/@xml:base" />
      </xsl:when>
      -->
      <!--
      <xsl:otherwise>
        <xsl:message>
          <xsl:text>WARNING: the $sourceURL parameter should be specified if no &lt;base&gt; element is present</xsl:text>
        </xsl:message>
      </xsl:otherwise>
      -->
    </xsl:choose>
  </xsl:variable>
  <rdf:RDF>
    <xsl:copy-of select="*/namespace::*" />
  	<xsl:apply-templates mode="RDFa2RDFXML">
      <xsl:with-param name="strContextBase" select="$strBase" />
    	<xsl:with-param name="strContextParentSubject" select="$strBase" />
    </xsl:apply-templates>
  </rdf:RDF>
</xsl:template>

<xsl:template match="*" mode="RDFa2RDFXML">
  <xsl:param name="strContextBase" />
  <xsl:param name="strContextParentSubject" />
  <xsl:param name="strContextLanguage" />
	<xsl:param name="strContextParentObject" />
	<xsl:param name="strContextForwardIncompleteTriples" />
	<xsl:param name="strContextBackwardIncompleteTriples" />
  
  <!--
  <xsl:if test=".//@property or .//@instanceof or .//@typeof or .//@rel or .//@rev or
                (($strContextForwardIncompleteTriples or 
                  $strContextBackwardIncompleteTriples) and
                 .//@href or .//@src or .//@resource or .//@about)">
  -->
	<xsl:variable name="nstCurrentElement" select="." />
  <xsl:variable name="nstURImappings" select="namespace::*" />
  <xsl:variable name="strLanguage">
    <xsl:choose>
      <xsl:when test="@xml:lang">
        <xsl:value-of select="@xml:lang" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$strContextLanguage" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <!--
  <xsl:variable name="strBase">
    <xsl:choose>
      <xsl:when test="@xml:base">
        <xsl:message>
          <xsl:text>WARNING: xml:base isn't allowed in XHTML+RDFa</xsl:text>
        </xsl:message>
        <xsl:call-template name="parseURI">
          <xsl:with-param name="strBaseURI" select="$strContextBase" />
          <xsl:with-param name="strRelativeURI" select="@xml:base" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$strContextBase" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  -->
  <xsl:variable name="strBase" select="$strContextBase" />
  
	<xsl:variable name="strAbout">
		<xsl:if test="@about">
			<xsl:call-template name="parseCURIEorURI">
				<xsl:with-param name="strCURIEorURI" select="normalize-space(@about)" />
				<xsl:with-param name="strBaseURI" select="$strBase" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
			</xsl:call-template>
		</xsl:if>
	</xsl:variable>
	<xsl:variable name="strSrc">
		<xsl:if test="@src">
			<xsl:call-template name="parseCURIEorURI">
				<xsl:with-param name="strCURIEorURI" select="normalize-space(@src)" />
				<xsl:with-param name="strBaseURI" select="$strBase" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
			</xsl:call-template>
		</xsl:if>
	</xsl:variable>
	<xsl:variable name="strResource">
		<xsl:if test="@resource">
			<xsl:call-template name="parseCURIEorURI">
				<xsl:with-param name="strCURIEorURI" select="normalize-space(@resource)" />
				<xsl:with-param name="strBaseURI" select="$strBase" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
			</xsl:call-template>
		</xsl:if>
	</xsl:variable>
	<xsl:variable name="strHref">
		<xsl:if test="@href">
			<xsl:call-template name="parseCURIEorURI">
				<xsl:with-param name="strCURIEorURI" select="normalize-space(@href)" />
				<xsl:with-param name="strBaseURI" select="$strBase" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
			</xsl:call-template>
		</xsl:if>
	</xsl:variable>
  
	<xsl:variable name="strRels">
		<xsl:if test="@rel">
			<xsl:call-template name="parseCURIEs">
				<xsl:with-param name="strCURIEs" select="normalize-space(@rel)" />
				<xsl:with-param name="strBaseURI" select="$strBase" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
				<xsl:with-param name="blnIncludeLinkTypes" select="true()" />
			</xsl:call-template>
		</xsl:if>
	</xsl:variable>
	<xsl:variable name="strRevs">
		<xsl:if test="@rev">
			<xsl:call-template name="parseCURIEs">
				<xsl:with-param name="strCURIEs" select="normalize-space(@rev)" />
				<xsl:with-param name="strBaseURI" select="$strBase" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
				<xsl:with-param name="blnIncludeLinkTypes" select="true()" />
			</xsl:call-template>
		</xsl:if>
	</xsl:variable>
  
	<xsl:variable name="strInstanceOfs">
	  <xsl:choose>
	    <xsl:when test="@typeof">
	      <xsl:call-template name="parseCURIEs">
	        <xsl:with-param name="strCURIEs" select="normalize-space(@typeof)" />
	        <xsl:with-param name="strBaseURI" select="$strBase" />
	        <xsl:with-param name="nstURImappings" select="$nstURImappings" />
	      </xsl:call-template>
	    </xsl:when>
	    <xsl:when test="@instanceof">
	      <xsl:message>WARNING: instanceof should be typeof</xsl:message>
	      <xsl:call-template name="parseCURIEs">
	        <xsl:with-param name="strCURIEs" select="normalize-space(@instanceof)" />
	        <xsl:with-param name="strBaseURI" select="$strBase" />
	        <xsl:with-param name="nstURImappings" select="$nstURImappings" />
	      </xsl:call-template>
	    </xsl:when>
	  </xsl:choose>
	</xsl:variable>
  
	<xsl:variable name="strNewSubject">
		<xsl:choose>
			<xsl:when test="@rel or @rev">
				<xsl:choose>
					<xsl:when test="@about">
						<xsl:value-of select="$strAbout" />
					</xsl:when>
					<xsl:when test="@src">
						<xsl:value-of select="$strSrc" />
					</xsl:when>
					<xsl:when test="$nstCurrentElement[self::h:head or self::h:body]">
						<xsl:value-of select="$strBase" />
					</xsl:when>
					<xsl:when test="@instanceof or @typeof">
						<xsl:value-of select="concat('_:', generate-id($nstCurrentElement))" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$strContextParentObject" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="@about">
				<xsl:value-of select="$strAbout" />
			</xsl:when>
			<xsl:when test="@src">
				<xsl:value-of select="$strSrc" />
			</xsl:when>
			<xsl:when test="@resource">
				<xsl:value-of select="$strResource" />
			</xsl:when>
			<xsl:when test="@href">
				<xsl:value-of select="$strHref" />
			</xsl:when>
			<xsl:when test="$nstCurrentElement[self::h:head or self::h:body]">
				<xsl:value-of select="$strBase" />
			</xsl:when>
			<xsl:when test="@instanceof or @typeof">
				<xsl:value-of select="concat('_:s', generate-id($nstCurrentElement))" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$strContextParentObject" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
  
  <xsl:variable name="strCurrentObjectResource">
		<xsl:if test="@rel or @rev">
			<xsl:choose>
				<xsl:when test="@resource">
					<xsl:value-of select="$strResource" />
				</xsl:when>
				<xsl:when test="@href">
					<xsl:value-of select="$strHref" />
				</xsl:when>
			</xsl:choose>
		</xsl:if>
	</xsl:variable>
  
	<xsl:if test="$strNewSubject != ''">
		<xsl:if test="$strInstanceOfs != ''">
			<xsl:call-template name="assignTypes">
				<xsl:with-param name="strSubject" select="$strNewSubject" />
				<xsl:with-param name="strTypes" select="$strInstanceOfs" />
			</xsl:call-template>
		</xsl:if>
	</xsl:if>
  
  <xsl:variable name="strParentSubject">
    <xsl:choose>
      <xsl:when test="$strNewSubject != ''">
        <xsl:value-of select="$strNewSubject" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$strContextParentSubject" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
	<xsl:if test="$strCurrentObjectResource != ''">
		<xsl:if test="$strRels != ''">
			<xsl:call-template name="completeStatements">
				<xsl:with-param name="strSubject" select="$strParentSubject" />
				<xsl:with-param name="strObject" select="$strCurrentObjectResource" />
				<xsl:with-param name="strPredicates" select="$strRels" />
			</xsl:call-template>
		</xsl:if>
		<xsl:if test="$strRevs != ''">
			<xsl:call-template name="completeStatements">
				<xsl:with-param name="strSubject" select="$strCurrentObjectResource" />
				<xsl:with-param name="strObject" select="$strParentSubject" />
				<xsl:with-param name="strPredicates" select="$strRevs" />
			</xsl:call-template>
		</xsl:if>
	</xsl:if>

	<xsl:variable name="strForwardIncompleteTriples">
		<xsl:variable name="strPredicates">
			<xsl:if test="$strCurrentObjectResource = ''">
				<xsl:value-of select="$strRels" />
			</xsl:if>
		</xsl:variable>
		<xsl:value-of select="normalize-space($strPredicates)" />
	</xsl:variable>
	<xsl:variable name="strBackwardIncompleteTriples">
		<xsl:variable name="strPredicates">
			<xsl:if test="$strCurrentObjectResource = ''">
				<xsl:value-of select="$strRevs" />
			</xsl:if>
		</xsl:variable>
		<xsl:value-of select="normalize-space($strPredicates)" />
	</xsl:variable>
  
	<xsl:variable name="strNewCurrentObjectResource">
		<xsl:choose>
			<xsl:when test="$strCurrentObjectResource = '' and
				              ($strRels != '' or $strRevs != '')">
				<xsl:value-of select="concat('_:o', generate-id($nstCurrentElement))" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$strCurrentObjectResource" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
  
  <xsl:variable name="strParentObject">
    <xsl:choose>
      <xsl:when test="$strNewCurrentObjectResource != ''">
        <xsl:value-of select="$strNewCurrentObjectResource" />
      </xsl:when>
      <xsl:when test="$strNewSubject != ''">
        <xsl:value-of select="$strNewSubject" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$strContextParentSubject" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
	<xsl:variable name="strProperties">
		<xsl:if test="@property">
			<xsl:call-template name="parseCURIEs">
				<xsl:with-param name="strCURIEs" select="normalize-space(@property)" />
				<xsl:with-param name="strBaseURI" select="$strBase" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
			</xsl:call-template>
		</xsl:if>
	</xsl:variable>
	
	<xsl:variable name="strSuppliedDatatype">
		<xsl:if test="@datatype">
			<xsl:call-template name="parseCURIE">
				<xsl:with-param name="strCURIE" select="normalize-space(@datatype)" />
				<xsl:with-param name="strBaseURI" select="$strBase" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
			</xsl:call-template>
		</xsl:if>
	</xsl:variable>
	<xsl:variable name="strDatatype">
		<xsl:choose>
			<xsl:when test="$strSuppliedDatatype != ''">
				<xsl:value-of select="$strSuppliedDatatype" />
			</xsl:when>
			<xsl:when test="@content or (@datatype and normalize-space(@datatype) = '')" />
			<xsl:when test="node()[not(self::text())]">
				<xsl:value-of select="$g_strRDFxmlLiteral" />
			</xsl:when>
		</xsl:choose>		
	</xsl:variable>
  
  <xsl:variable name="strCurrentObjectLiteral">
    <xsl:choose>
      <xsl:when test="@content">
        <!-- Note no normalisation of whitespace here; it might be significant -->
        <xsl:value-of select="@content" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="." />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
	<xsl:if test="$strProperties != ''">
		<xsl:choose>
			<xsl:when test="$strDatatype = $g_strRDFxmlLiteral">
				<xsl:call-template name="completeXMLLiteralStatements">
					<xsl:with-param name="strSubject" select="$strParentSubject" />
					<xsl:with-param name="rtfObject">
					  <xsl:for-each select="node()">
					    <xsl:choose>
					      <xsl:when test="self::*">
					        <xsl:copy>
					          <xsl:if test="$strLanguage != ''">
					            <xsl:attribute name="xml:lang">
					              <xsl:value-of select="$strLanguage" />
					            </xsl:attribute>
					          </xsl:if>
					          <xsl:copy-of select="@*" />
					          <xsl:copy-of select="node()" />
					        </xsl:copy>
					      </xsl:when>
					      <xsl:otherwise>
					        <xsl:copy-of select="." />
					      </xsl:otherwise>
					    </xsl:choose>
					  </xsl:for-each>
					</xsl:with-param>
					<xsl:with-param name="strPredicates" select="$strProperties" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="completeLiteralStatements">
					<xsl:with-param name="strSubject" select="$strParentSubject" />
					<xsl:with-param name="strObject" select="$strCurrentObjectLiteral" />
					<xsl:with-param name="strPredicates" select="$strProperties" />
					<xsl:with-param name="strDatatype" select="$strDatatype" />
					<xsl:with-param name="strLanguage" select="$strLanguage" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:if>
  
  <xsl:variable name="blnSkipElement"
    select="$strProperties = '' and
            $strRels = '' and $strRevs = '' and
            $strAbout = '' and $strSrc = '' and
            $strResource = '' and $strHref = '' and
            $nstCurrentElement[not(self::h:head or self::h:body)] and
            $strInstanceOfs = ''" />
  
  <xsl:if test="$debug">
    <xsl:message>
      <xsl:value-of select="name($nstCurrentElement)"/>
      context:
        base: <xsl:value-of select="$strContextBase"/>
        parent subject: <xsl:value-of select="$strContextParentSubject"/>
        parent object: <xsl:value-of select="$strContextParentObject"/>
        forward incomplete triples: <xsl:value-of select="$strContextForwardIncompleteTriples"/>
        backward incomplete triples: <xsl:value-of select="$strContextBackwardIncompleteTriples"/>
      attributes:
        about: <xsl:value-of select="$strAbout"/>
        href: <xsl:value-of select="$strHref"/>
        src: <xsl:value-of select="$strSrc"/>
        resource: <xsl:value-of select="$strResource"/>
        types: <xsl:value-of select="$strInstanceOfs"/>
        rels: <xsl:value-of select="$strRels"/>
        revs: <xsl:value-of select="$strRevs"/>
        properties: <xsl:value-of select="$strProperties"/>
        datatype: <xsl:value-of select="$strSuppliedDatatype"/>
      calculated values:
        new subject: <xsl:value-of select="$strNewSubject"/>
        original current object resource: <xsl:value-of select="$strCurrentObjectResource"/>
        new current object resource: <xsl:value-of select="$strNewCurrentObjectResource"/>
        forward incomplete triples: <xsl:value-of select="$strForwardIncompleteTriples"/>
        backward incomplete triples: <xsl:value-of select="$strBackwardIncompleteTriples"/>
        current object literal: <xsl:value-of select="$strCurrentObjectLiteral"/>
        datatype: <xsl:value-of select="$strDatatype"/>
        skip element?: <xsl:value-of select="$blnSkipElement"/>
    </xsl:message>
  </xsl:if>
  
	<xsl:if test="not($blnSkipElement) and $strNewSubject != ''">
		<xsl:if test="$strContextForwardIncompleteTriples != ''">
			<xsl:call-template name="completeStatements">
				<xsl:with-param name="strSubject" select="$strContextParentSubject" />
				<xsl:with-param name="strObject" select="$strNewSubject" />
				<xsl:with-param name="strPredicates" select="$strContextForwardIncompleteTriples" />
			</xsl:call-template>
		</xsl:if>
		<xsl:if test="$strContextBackwardIncompleteTriples != ''">
			<xsl:call-template name="completeStatements">
				<xsl:with-param name="strSubject" select="$strNewSubject" />
				<xsl:with-param name="strObject" select="$strContextParentSubject" />
				<xsl:with-param name="strPredicates" select="$strContextBackwardIncompleteTriples" />
			</xsl:call-template>
		</xsl:if>
	</xsl:if>
  
  <xsl:if test="$strProperties = '' or $strDatatype != $g_strRDFxmlLiteral">
    <xsl:choose>
      <xsl:when test="$blnSkipElement">
        <xsl:apply-templates select="*" mode="RDFa2RDFXML">
          <xsl:with-param name="strContextBase" select="$strBase" />
          <xsl:with-param name="strContextParentSubject" select="$strContextParentSubject" />
          <xsl:with-param name="strContextLanguage" select="$strLanguage" />
          <xsl:with-param name="strContextParentObject" select="$strContextParentObject" />
          <xsl:with-param name="strContextForwardIncompleteTriples" select="$strContextForwardIncompleteTriples" />
          <xsl:with-param name="strContextBackwardIncompleteTriples" select="$strContextBackwardIncompleteTriples" />
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="*" mode="RDFa2RDFXML">
          <xsl:with-param name="strContextBase" select="$strBase" />
          <xsl:with-param name="strContextParentSubject" select="$strParentSubject" />
          <xsl:with-param name="strContextLanguage" select="$strLanguage" />
          <xsl:with-param name="strContextParentObject" select="$strParentObject" />
          <xsl:with-param name="strContextForwardIncompleteTriples" select="$strForwardIncompleteTriples" />
          <xsl:with-param name="strContextBackwardIncompleteTriples" select="$strBackwardIncompleteTriples" />
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
  
  <!--</xsl:if>-->
</xsl:template>

<xsl:template name="assignTypes">
	<xsl:param name="strSubject" />
	<xsl:param name="strTypes" />
	<xsl:choose>
		<xsl:when test="contains($strTypes, ' ')">
			<xsl:call-template name="makeStatement">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="strObjectResource" select="substring-before($strTypes, ' ')" />
				<xsl:with-param name="strPredicate" select="$g_strRDFtype" />
			</xsl:call-template>
			<xsl:call-template name="assignTypes">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="strTypes" select="substring-after($strTypes, ' ')" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="makeStatement">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="strObjectResource" select="$strTypes" />
				<xsl:with-param name="strPredicate" select="$g_strRDFtype" />
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>	
</xsl:template>
	
<xsl:template name="completeLiteralStatements">
	<xsl:param name="strSubject" />
	<xsl:param name="strObject" />
	<xsl:param name="strLanguage" />
	<xsl:param name="strDatatype" />
	<xsl:param name="strPredicates" />
	<xsl:choose>
		<xsl:when test="contains($strPredicates, ' ')">
			<xsl:call-template name="makeStatement">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="strObjectLiteral" select="$strObject" />
				<xsl:with-param name="strObjectDatatype" select="$strDatatype" />
				<xsl:with-param name="strObjectLanguage" select="$strLanguage" />
				<xsl:with-param name="strPredicate" select="substring-before($strPredicates, ' ')" />
			</xsl:call-template>
		  <xsl:call-template name="completeLiteralStatements">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="strObject" select="$strObject" />
				<xsl:with-param name="strLanguage" select="$strLanguage" />
				<xsl:with-param name="strDatatype" select="$strDatatype" />
				<xsl:with-param name="strPredicates" select="substring-after($strPredicates, ' ')" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="makeStatement">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="strObjectLiteral" select="$strObject" />
				<xsl:with-param name="strObjectDatatype" select="$strDatatype" />
				<xsl:with-param name="strObjectLanguage" select="$strLanguage" />
				<xsl:with-param name="strPredicate" select="$strPredicates" />
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>	
</xsl:template>
	
<xsl:template name="completeXMLLiteralStatements">
	<xsl:param name="strSubject" />
	<xsl:param name="rtfObject" />
	<xsl:param name="strPredicates" />
	<xsl:choose>
		<xsl:when test="contains($strPredicates, ' ')">
			<xsl:call-template name="makeStatement">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="rtfObjectXMLLiteral" select="$rtfObject" />
				<xsl:with-param name="strPredicate" select="substring-before($strPredicates, ' ')" />
			</xsl:call-template>
			<xsl:call-template name="completeXMLLiteralStatements">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="rtfObject" select="$rtfObject" />
				<xsl:with-param name="strPredicates" select="substring-after($strPredicates, ' ')" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="makeStatement">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="rtfObjectXMLLiteral" select="$rtfObject" />
				<xsl:with-param name="strPredicate" select="$strPredicates" />
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>	
</xsl:template>

<xsl:template name="completeStatements">
	<xsl:param name="strSubject" />
	<xsl:param name="strObject" />
	<xsl:param name="strPredicates" />
	<xsl:choose>
		<xsl:when test="contains($strPredicates, ' ')">
			<xsl:call-template name="makeStatement">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="strObjectResource" select="$strObject" />
				<xsl:with-param name="strPredicate" select="substring-before($strPredicates, ' ')" />
			</xsl:call-template>
			<xsl:call-template name="completeStatements">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="strObject" select="$strObject" />
				<xsl:with-param name="strPredicates" select="substring-after($strPredicates, ' ')" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="makeStatement">
				<xsl:with-param name="strSubject" select="$strSubject" />
				<xsl:with-param name="strObjectResource" select="$strObject" />
				<xsl:with-param name="strPredicate" select="$strPredicates" />
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>	
</xsl:template>

<xsl:template name="makeStatement">
	<xsl:param name="strSubject" />
	<xsl:param name="strObjectResource" />
	<xsl:param name="strPredicate" />
	<xsl:param name="strObjectLiteral" />
	<xsl:param name="rtfObjectXMLLiteral" />
	<xsl:param name="strObjectDatatype" />
	<xsl:param name="strObjectLanguage" />
	<rdf:Statement>
		<rdf:subject>
			<xsl:choose>
				<xsl:when test="starts-with($strSubject, '_:')">
					<xsl:attribute name="rdf:nodeID">
						<xsl:value-of select="substring-after($strSubject, '_:')" />
					</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="rdf:resource">
						<xsl:value-of select="$strSubject" />
					</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
		</rdf:subject>
		<rdf:predicate rdf:resource="{$strPredicate}" />
		<rdf:object>
			<xsl:choose>
				<xsl:when test="$strObjectResource != ''">
					<xsl:choose>
						<xsl:when test="starts-with($strObjectResource, '_:')">
							<xsl:attribute name="rdf:nodeID">
								<xsl:value-of select="substring-after($strObjectResource, '_:')" />
							</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="rdf:resource">
								<xsl:value-of select="$strObjectResource" />
							</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$strObjectLiteral != ''">
					<xsl:if test="$strObjectLanguage != ''">
						<xsl:attribute name="xml:lang">
							<xsl:value-of select="$strObjectLanguage" />
						</xsl:attribute>
					</xsl:if>
					<xsl:if test="$strObjectDatatype != ''">
						<xsl:attribute name="rdf:datatype">
							<xsl:value-of select="$strObjectDatatype" />
						</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="$strObjectLiteral" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="rdf:parseType">Literal</xsl:attribute>
					<xsl:copy-of select="$rtfObjectXMLLiteral" />
				</xsl:otherwise>
			</xsl:choose>			
		</rdf:object>
	</rdf:Statement>
</xsl:template>

<!-- *** Parsing CURIEs and URIs *** -->

<xsl:template name="parseCURIEorURI">
  <xsl:param name="strCURIEorURI" />
  <xsl:param name="strBaseURI" />
  <xsl:param name="nstURImappings" />
  <xsl:choose>
    <xsl:when test="starts-with($strCURIEorURI, '[')">
    	<!-- strip off the []s that wrap around the CURIE -->
      <xsl:call-template name="parseCURIE">
        <xsl:with-param name="strCURIE" select="substring($strCURIEorURI, 2, string-length($strCURIEorURI) - 2)" />
      	<xsl:with-param name="strBaseURI" select="$strBaseURI" />
        <xsl:with-param name="nstURImappings" select="$nstURImappings" />
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="parseURI">
        <xsl:with-param name="strRelativeURI" select="$strCURIEorURI" />
        <xsl:with-param name="strBaseURI" select="$strBaseURI" />
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="parseCURIEs">
	<xsl:param name="strCURIEs" />
	<xsl:param name="strBaseURI" />
	<xsl:param name="nstURImappings" />
	<xsl:param name="blnIncludeLinkTypes" />
	<xsl:choose>
		<xsl:when test="contains($strCURIEs, ' ')">
			<xsl:call-template name="parseCURIE">
				<xsl:with-param name="strCURIE" select="substring-before($strCURIEs, ' ')" />
				<xsl:with-param name="strBaseURI" select="$strBaseURI" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
				<xsl:with-param name="blnIncludeLinkTypes" select="$blnIncludeLinkTypes" />
			</xsl:call-template>
			<xsl:text> </xsl:text>
			<xsl:call-template name="parseCURIEs">
				<xsl:with-param name="strCURIEs" select="substring-after($strCURIEs, ' ')" />
				<xsl:with-param name="strBaseURI" select="$strBaseURI" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
				<xsl:with-param name="blnIncludeLinkTypes" select="$blnIncludeLinkTypes" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="parseCURIE">
				<xsl:with-param name="strCURIE" select="$strCURIEs" />
				<xsl:with-param name="strBaseURI" select="$strBaseURI" />
				<xsl:with-param name="nstURImappings" select="$nstURImappings" />
				<xsl:with-param name="blnIncludeLinkTypes" select="$blnIncludeLinkTypes" />
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>	
</xsl:template>

<xsl:template name="parseCURIE">
  <xsl:param name="strCURIE" />
	<xsl:param name="strBaseURI" />
  <xsl:param name="nstURImappings" />
	<xsl:param name="blnIncludeLinkTypes" />
	<xsl:choose>
		<xsl:when test="contains($strCURIE, ':')">
			<xsl:variable name="strPrefix" select="substring-before($strCURIE, ':')" />
			<xsl:variable name="strReference" select="substring-after($strCURIE, ':')" />
		  <xsl:choose>
		    <xsl:when test="$strPrefix = '_'">
		      <xsl:choose>
		        <xsl:when test="$strReference">
		          <xsl:value-of select="$strCURIE" />
		        </xsl:when>
		        <xsl:otherwise>
		          <xsl:value-of select="concat('_:', $g_strDefaultBNode)" />
		        </xsl:otherwise>
		      </xsl:choose>
		    </xsl:when>
		    <xsl:otherwise>
		      <xsl:variable name="strURI">
		        <xsl:choose>
		          <xsl:when test="$strPrefix = ''">
		            <xsl:value-of select="concat($g_strHTMLvocab, $strReference)" />
		          </xsl:when>
		          <xsl:otherwise>
		            <xsl:variable name="nstBase" select="$nstURImappings[name() = $strPrefix]" />
		            <xsl:choose>
		              <xsl:when test="$nstBase">
		                <xsl:value-of select="concat($nstBase, $strReference)" />
		              </xsl:when>
		              <xsl:otherwise>
		                <xsl:message>
		                  <xsl:text>ERROR in CURIE; no namespace mapping for "</xsl:text>
		                  <xsl:value-of select="$strPrefix" />
		                  <xsl:text>" prefix in "</xsl:text>
		                  <xsl:value-of select="$strCURIE" />
		                  <xsl:text>"</xsl:text>
		                </xsl:message>
		              </xsl:otherwise>
		            </xsl:choose>
		          </xsl:otherwise>
		        </xsl:choose>
		      </xsl:variable>
		      <xsl:call-template name="parseURI">
		        <xsl:with-param name="strRelativeURI" select="$strURI" />
		        <xsl:with-param name="strBaseURI" select="$strBaseURI" />
		      </xsl:call-template>
		    </xsl:otherwise>
		  </xsl:choose>
		</xsl:when>
		<xsl:when test="$blnIncludeLinkTypes">
			<xsl:if test="contains($g_strHTMLlinkTypes, concat(' ', $strCURIE, ' '))">
				<xsl:value-of select="concat($g_strHTMLvocab, $strCURIE)" />
			</xsl:if>
		</xsl:when>
		<!-- otherwise this isn't a valid CURIE, so ignore it -->
	</xsl:choose>	
</xsl:template>

<!-- *** Resolving URIs *** -->

<!-- This code is based on http://www.ietf.org/rfc/rfc3986.txt, Section 5.2 -->
<xsl:template name="parseURI">
  <xsl:param name="strRelativeURI" />
  <xsl:param name="strBaseURI" />
  <xsl:choose>
    <xsl:when test="$strBaseURI = ''">
      <xsl:value-of select="$strRelativeURI" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="strRelFragment"
        select="substring-after($strRelativeURI, '#')" />
      <xsl:variable name="strRelBeforeFragment">
        <xsl:choose>
          <xsl:when test="contains($strRelativeURI, '#')">
            <xsl:value-of select="substring-before($strRelativeURI, '#')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$strRelativeURI" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      
      <xsl:variable name="strRelQuery"
        select="substring-after($strRelBeforeFragment, '?')" />
      <xsl:variable name="strRelBeforeQuery">
        <xsl:choose>
          <xsl:when test="contains($strRelBeforeFragment, '?')">
            <xsl:value-of select="substring-before($strRelBeforeFragment, '?')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$strRelBeforeFragment" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      
      <xsl:variable name="strRelFirstPart">
        <xsl:choose>
          <xsl:when test="contains($strRelBeforeQuery, '/')">
            <xsl:value-of select="substring-before($strRelBeforeQuery, '/')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$strRelBeforeQuery" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="strRelScheme"
        select="substring-before($strRelFirstPart, ':')" />
      <xsl:variable name="strRelAfterScheme">
        <xsl:choose>
          <xsl:when test="$strRelScheme = ''">
            <xsl:value-of select="$strRelBeforeQuery" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="substring-after($strRelBeforeQuery, ':')" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      
      <xsl:variable name="strRelAuthority">
        <xsl:if test="starts-with($strRelAfterScheme, '//')">
          <xsl:variable name="strRelAfterSlashes"
            select="substring($strRelAfterScheme, 3)" />
          <xsl:choose>
            <xsl:when test="contains($strRelAfterSlashes, '/')">
              <xsl:value-of select="substring-before($strRelAfterSlashes, '/')" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$strRelAfterSlashes" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:variable>
      
      <xsl:variable name="strRelPath">
        <xsl:choose>
          <xsl:when test="starts-with($strRelAfterScheme, '//')">
            <xsl:variable name="strRelAfterSlashes"
              select="substring($strRelAfterScheme, 3)" />
            <xsl:if test="contains($strRelAfterSlashes, '/')">
              <xsl:value-of select="concat('/', substring-after($strRelAfterSlashes, '/'))" />
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$strRelAfterScheme" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$strRelScheme != ''">
          <!-- absolute URI -->
          <xsl:call-template name="constructURI">
            <xsl:with-param name="strScheme" select="$strRelScheme" />
            <xsl:with-param name="strAuthority" select="$strRelAuthority" />
            <xsl:with-param name="strPath">
              <xsl:call-template name="uriRemoveDotSegments">
                <xsl:with-param name="strPath" select="$strRelPath" />
              </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="strQuery" select="$strRelQuery" />
            <xsl:with-param name="strFragment" select="$strRelFragment" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <!-- relative URI -->
          <xsl:variable name="strBaseFragment"
            select="substring-after($strBaseURI, '#')" />
          <xsl:variable name="strBaseBeforeFragment">
            <xsl:choose>
              <xsl:when test="contains($strBaseURI, '#')">
                <xsl:value-of select="substring-before($strBaseURI, '#')" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$strBaseURI" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          
          <xsl:variable name="strBaseQuery"
            select="substring-after($strBaseBeforeFragment, '?')" />
          <xsl:variable name="strBaseBeforeQuery">
            <xsl:choose>
              <xsl:when test="contains($strBaseBeforeFragment, '?')">
                <xsl:value-of select="substring-before($strBaseBeforeFragment, '?')" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$strBaseBeforeFragment" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          
          <xsl:variable name="strBaseScheme"
            select="substring-before($strBaseBeforeQuery, ':')" />
          <xsl:variable name="strBaseAfterScheme"
            select="substring-after($strBaseBeforeQuery, ':')" />
          
          <xsl:variable name="strBaseAuthority">
            <xsl:if test="starts-with($strBaseAfterScheme, '//')">
              <xsl:variable name="strBaseAfterSlashes"
                select="substring($strBaseAfterScheme, 3)" />
              <xsl:choose>
                <xsl:when test="contains($strBaseAfterSlashes, '/')">
                  <xsl:value-of select="substring-before($strBaseAfterSlashes, '/')" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$strBaseAfterSlashes" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:variable>
          
          <xsl:variable name="strBasePath">
            <xsl:choose>
              <xsl:when test="starts-with($strBaseAfterScheme, '//')">
                <xsl:variable name="strBaseAfterSlashes"
                  select="substring($strBaseAfterScheme, 3)" />
                <xsl:if test="contains($strBaseAfterSlashes, '/')">
                  <xsl:value-of select="concat('/', substring-after($strBaseAfterSlashes, '/'))" />
                </xsl:if>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$strBaseAfterScheme" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          
          <xsl:choose>
            <xsl:when test="$strRelAuthority != ''">
              <xsl:call-template name="constructURI">
                <xsl:with-param name="strScheme" select="$strBaseScheme" />
                <xsl:with-param name="strAuthority" select="$strRelAuthority" />
                <xsl:with-param name="strPath">
                  <xsl:call-template name="uriRemoveDotSegments">
                    <xsl:with-param name="strPath" select="$strRelPath" />
                  </xsl:call-template>
                </xsl:with-param>
                <xsl:with-param name="strQuery" select="$strRelQuery" />
                <xsl:with-param name="strFragment" select="$strRelFragment" />
              </xsl:call-template>
            </xsl:when>
            <xsl:when test="$strRelPath = ''">
              <xsl:call-template name="constructURI">
                <xsl:with-param name="strScheme" select="$strBaseScheme" />
                <xsl:with-param name="strAuthority" select="$strBaseAuthority" />
                <xsl:with-param name="strPath" select="$strBasePath" />
                <xsl:with-param name="strQuery">
                  <xsl:choose>
                    <xsl:when test="$strRelQuery = ''">
                      <xsl:value-of select="$strBaseQuery" />
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="$strRelQuery" />
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="strFragment" select="$strRelFragment" />
              </xsl:call-template>
            </xsl:when>
            <xsl:when test="starts-with($strRelPath, '/')">
              <xsl:call-template name="constructURI">
                <xsl:with-param name="strScheme" select="$strBaseScheme" />
                <xsl:with-param name="strAuthority" select="$strBaseAuthority" />
                <xsl:with-param name="strPath">
                  <xsl:call-template name="uriRemoveDotSegments">
                    <xsl:with-param name="strPath" select="$strRelPath" />
                  </xsl:call-template>
                </xsl:with-param>
                <xsl:with-param name="strQuery" select="$strRelQuery" />
                <xsl:with-param name="strFragment" select="$strRelFragment" />
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="constructURI">
                <xsl:with-param name="strScheme" select="$strBaseScheme" />
                <xsl:with-param name="strAuthority" select="$strBaseAuthority" />
                <xsl:with-param name="strPath">
                  <xsl:call-template name="uriRemoveDotSegments">
                    <xsl:with-param name="strPath">
                      <xsl:call-template name="uriMergePaths">
                        <xsl:with-param name="strBaseAuthority" select="$strBaseAuthority" />
                        <xsl:with-param name="strBasePath" select="$strBasePath" />
                        <xsl:with-param name="strRelPath" select="$strRelPath" />
                      </xsl:call-template>
                    </xsl:with-param>
                  </xsl:call-template>
                </xsl:with-param>
                <xsl:with-param name="strQuery" select="$strRelQuery" />
                <xsl:with-param name="strFragment" select="$strRelFragment" />
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="constructURI">
  <xsl:param name="strScheme" />
  <xsl:param name="strAuthority" />
  <xsl:param name="strPath" />
  <xsl:param name="strQuery" />
  <xsl:param name="strFragment" />
  <xsl:value-of select="$strScheme" />
  <xsl:text>:</xsl:text>
  <xsl:if test="$strAuthority != ''">
    <xsl:text>//</xsl:text>
  	<xsl:value-of select="$strAuthority" />
  </xsl:if>
  <xsl:value-of select="$strPath" />
  <xsl:if test="$strQuery != ''">
    <xsl:text>?</xsl:text>
    <xsl:value-of select="$strQuery" />
  </xsl:if>
  <xsl:if test="$strFragment != ''">
    <xsl:text>#</xsl:text>
  	<xsl:value-of select="$strFragment" />
  </xsl:if>
</xsl:template>

<xsl:template name="uriMergePaths">
	<xsl:param name="strBaseAuthority" />
	<xsl:param name="strBasePath" />
	<xsl:param name="strRelPath" />
	<xsl:choose>
		<xsl:when test="$strBaseAuthority != '' and $strBasePath = ''">
			<xsl:value-of select="concat('/', $strRelPath)" />
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="uriRemoveLastSegment">
				<xsl:with-param name="strPath" select="$strBasePath" />
				<xsl:with-param name="blnRemoveTrailingSlash" select="false()" />
			</xsl:call-template>
			<xsl:value-of select="$strRelPath" />
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="uriRemoveDotSegments">
  <xsl:param name="strPath" />
  <xsl:param name="strResult" select="''" />
  <xsl:choose>
  	<xsl:when test="not(contains($strPath, '.'))">
  		<xsl:value-of select="concat($strResult, $strPath)" />
  	</xsl:when>
    <xsl:when test="$strPath = '' or $strPath = '.' or $strPath = '..'">
      <xsl:value-of select="$strResult" />
    </xsl:when>
    <xsl:when test="starts-with($strPath, '../') or
                    starts-with($strPath, './')">
      <xsl:call-template name="uriRemoveDotSegments">
        <xsl:with-param name="strPath" select="substring-after($strPath, '/')" />
        <xsl:with-param name="strResult" select="$strResult" />
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="starts-with($strPath, '/./')">
      <xsl:call-template name="uriRemoveDotSegments">
        <xsl:with-param name="strPath" select="substring($strPath, 3)" />
        <xsl:with-param name="strResult" select="$strResult" />
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="$strPath = '/.'">
      <xsl:call-template name="uriRemoveDotSegments">
        <xsl:with-param name="strPath" select="concat('/', substring($strPath, 3))" />
        <xsl:with-param name="strResult" select="$strResult" />
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="starts-with($strPath, '/../')">
      <xsl:call-template name="uriRemoveDotSegments">
        <xsl:with-param name="strPath" select="substring($strPath, 4)" />
        <xsl:with-param name="strResult">
          <xsl:call-template name="uriRemoveLastSegment">
            <xsl:with-param name="strPath" select="$strResult" />
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="starts-with($strPath, '/..')">
      <xsl:call-template name="uriRemoveDotSegments">
        <xsl:with-param name="strPath" select="concat('/', substring($strPath, 4))" />
        <xsl:with-param name="strResult">
          <xsl:call-template name="uriRemoveLastSegment">
            <xsl:with-param name="strPath" select="$strResult" />
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="starts-with($strPath, '/')">
      <xsl:variable name="strFirstSegment">
        <xsl:variable name="strAfterSlash" select="substring($strPath, 2)" />
        <xsl:text>/</xsl:text>
        <xsl:choose>
          <xsl:when test="contains($strAfterSlash, '/')">
            <xsl:value-of select="substring-before($strAfterSlash, '/')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$strAfterSlash" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:call-template name="uriRemoveDotSegments">
        <xsl:with-param name="strPath" select="substring-after($strPath, $strFirstSegment)" />
        <xsl:with-param name="strResult" select="concat($strResult, $strFirstSegment)" />
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="strFirstSegment">
        <xsl:choose>
          <xsl:when test="contains($strPath, '/')">
            <xsl:value-of select="substring-before($strPath, '/')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$strPath" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:call-template name="uriRemoveDotSegments">
        <xsl:with-param name="strPath" select="substring-after($strPath, $strFirstSegment)" />
        <xsl:with-param name="strResult" select="concat($strResult, $strFirstSegment)" />
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="uriRemoveLastSegment">
  <xsl:param name="strPath" />
	<xsl:param name="strResult" />
	<xsl:param name="blnRemoveTrailingSlash" select="true()" />
  <xsl:choose>
  	<xsl:when test="contains($strPath, '/')">
  		<xsl:call-template name="uriRemoveLastSegment">
  			<xsl:with-param name="strPath" select="substring-after($strPath, '/')" />
  			<xsl:with-param name="strResult" select="concat($strResult, substring-before($strPath, '/'), '/')" />
  		  <xsl:with-param name="blnRemoveTrailingSlash" select="$blnRemoveTrailingSlash" />
  		</xsl:call-template>
  	</xsl:when>
  	<xsl:otherwise>
  		<xsl:choose>
  			<xsl:when test="$blnRemoveTrailingSlash and 
  				              substring($strResult, string-length($strResult)) = '/'">
  				<xsl:value-of select="substring($strResult, 1, string-length($strResult) - 1)" />
  			</xsl:when>
  			<xsl:otherwise>
  				<xsl:value-of select="$strResult" />
  			</xsl:otherwise>
  		</xsl:choose>
  	</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<msxsl:script language="JScript" implements-prefix="exsl">
	this['node-set'] =  function (x) {
	return x;
	}
</msxsl:script>
	

</xsl:stylesheet>
