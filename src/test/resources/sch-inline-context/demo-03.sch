<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    
    <sch:pattern>
        
        <sch:rule context="title">
            <sch:assert test="following-sibling::p" id="a0001">
                title should be followed by a paragraph
            </sch:assert>
        </sch:rule>
        
        
    </sch:pattern>
    
</sch:schema>