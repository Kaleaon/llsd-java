/*!
 * LLSD XML Parser and Serializer - Rust Implementation
 * 
 * Based on Java implementation and Second Life viewer XML handling
 * Copyright (C) 2024 Linden Lab
 */

use crate::types::{LLSDValue, LLSDDocument};
use crate::error::{LLSDError, LLSDResult};
use quick_xml::events::{Event, BytesEnd, BytesStart, BytesText};
use quick_xml::{Reader, Writer};
use std::collections::HashMap;
use std::io::Cursor;
use uuid::Uuid;
use chrono::{DateTime, Utc};

/// LLSD XML parser
#[derive(Debug, Default)]
pub struct LLSDXmlParser {
    validate_structure: bool,
}

impl LLSDXmlParser {
    /// Create a new XML parser
    pub fn new() -> Self {
        Self::default()
    }

    /// Enable XML structure validation
    pub fn with_validation(mut self, validate: bool) -> Self {
        self.validate_structure = validate;
        self
    }

    /// Parse LLSD from XML string
    pub fn parse(&self, xml: &str) -> LLSDResult<LLSDDocument> {
        let mut reader = Reader::from_str(xml);
        reader.trim_text(true);
        
        let mut buf = Vec::new();
        let mut found_llsd_root = false;
        
        // Find the LLSD root element
        loop {
            match reader.read_event(&mut buf) {
                Ok(Event::Start(ref e)) => {
                    if e.name() == b"llsd" {
                        found_llsd_root = true;
                        break;
                    }
                }
                Ok(Event::Eof) => break,
                Err(e) => return Err(LLSDError::from(e)),
                _ => {}
            }
        }

        if !found_llsd_root {
            return Err(LLSDError::custom("Missing <llsd> root element"));
        }

        // Parse the first child element
        let value = self.parse_element(&mut reader, &mut buf)?;
        Ok(LLSDDocument::new(value))
    }

    /// Parse an individual XML element
    fn parse_element(&self, reader: &mut Reader<&[u8]>, buf: &mut Vec<u8>) -> LLSDResult<LLSDValue> {
        loop {
            match reader.read_event(buf) {
                Ok(Event::Start(ref e)) => {
                    let tag_name = String::from_utf8_lossy(e.name());
                    return self.parse_typed_element(&tag_name, reader, buf);
                }
                Ok(Event::Empty(ref e)) => {
                    let tag_name = String::from_utf8_lossy(e.name());
                    return self.parse_empty_element(&tag_name);
                }
                Ok(Event::End(_)) => {
                    return Ok(LLSDValue::Undefined);
                }
                Ok(Event::Eof) => {
                    return Ok(LLSDValue::Undefined);
                }
                Err(e) => return Err(LLSDError::from(e)),
                _ => {}
            }
        }
    }

    /// Parse a typed XML element with content
    fn parse_typed_element(&self, tag_name: &str, reader: &mut Reader<&[u8]>, buf: &mut Vec<u8>) -> LLSDResult<LLSDValue> {
        match tag_name {
            "undef" => {
                self.skip_to_end(reader, buf, "undef")?;
                Ok(LLSDValue::Undefined)
            }
            "boolean" => {
                let content = self.read_text_content(reader, buf)?;
                let value = content.trim().to_lowercase();
                Ok(LLSDValue::Boolean(value == "1" || value == "true"))
            }
            "integer" => {
                let content = self.read_text_content(reader, buf)?;
                let value: i32 = content.trim().parse()
                    .map_err(|_| LLSDError::custom(format!("Invalid integer: {}", content)))?;
                Ok(LLSDValue::Integer(value))
            }
            "real" => {
                let content = self.read_text_content(reader, buf)?;
                let value: f64 = content.trim().parse()
                    .map_err(|_| LLSDError::custom(format!("Invalid real: {}", content)))?;
                Ok(LLSDValue::Real(value))
            }
            "string" => {
                let content = self.read_text_content(reader, buf)?;
                Ok(LLSDValue::String(content))
            }
            "uuid" => {
                let content = self.read_text_content(reader, buf)?;
                let uuid = Uuid::parse_str(content.trim())
                    .map_err(|_| LLSDError::InvalidUuid { uuid: content })?;
                Ok(LLSDValue::UUID(uuid))
            }
            "date" => {
                let content = self.read_text_content(reader, buf)?;
                let date = DateTime::parse_from_rfc3339(content.trim())
                    .map_err(|_| LLSDError::InvalidDate { date: content.clone() })?
                    .with_timezone(&Utc);
                Ok(LLSDValue::Date(date))
            }
            "uri" => {
                let content = self.read_text_content(reader, buf)?;
                Ok(LLSDValue::URI(content))
            }
            "binary" => {
                let content = self.read_text_content(reader, buf)?;
                let bytes = base64::decode(content.trim())?;
                Ok(LLSDValue::Binary(bytes))
            }
            "array" => self.parse_array(reader, buf),
            "map" => self.parse_map(reader, buf),
            _ => Err(LLSDError::custom(format!("Unknown LLSD element: {}", tag_name)))
        }
    }

    /// Parse empty XML elements
    fn parse_empty_element(&self, tag_name: &str) -> LLSDResult<LLSDValue> {
        match tag_name {
            "undef" => Ok(LLSDValue::Undefined),
            "string" => Ok(LLSDValue::String(String::new())),
            "binary" => Ok(LLSDValue::Binary(Vec::new())),
            "array" => Ok(LLSDValue::Array(Vec::new())),
            "map" => Ok(LLSDValue::Map(HashMap::new())),
            "uuid" => Ok(LLSDValue::UUID(Uuid::nil())),
            _ => Err(LLSDError::custom(format!("Cannot have empty element: {}", tag_name)))
        }
    }

    /// Read text content from an element
    fn read_text_content(&self, reader: &mut Reader<&[u8]>, buf: &mut Vec<u8>) -> LLSDResult<String> {
        let mut content = String::new();
        
        loop {
            match reader.read_event(buf) {
                Ok(Event::Text(ref e)) => {
                    content.push_str(&e.unescape_and_decode(reader)?);
                }
                Ok(Event::CData(ref e)) => {
                    content.push_str(&String::from_utf8_lossy(&e));
                }
                Ok(Event::End(_)) => break,
                Ok(Event::Eof) => break,
                Err(e) => return Err(LLSDError::from(e)),
                _ => {}
            }
        }
        
        Ok(content)
    }

    /// Parse an array element
    fn parse_array(&self, reader: &mut Reader<&[u8]>, buf: &mut Vec<u8>) -> LLSDResult<LLSDValue> {
        let mut array = Vec::new();
        
        loop {
            match reader.read_event(buf) {
                Ok(Event::Start(_)) | Ok(Event::Empty(_)) => {
                    // Step back one event to re-parse the element
                    reader.read_to_end(b"dummy", buf)?; // This is a hack to get the position right
                    // TODO: Implement proper position tracking
                    let element = self.parse_element(reader, buf)?;
                    array.push(element);
                }
                Ok(Event::End(ref e)) if e.name() == b"array" => break,
                Ok(Event::Eof) => break,
                Err(e) => return Err(LLSDError::from(e)),
                _ => {}
            }
        }
        
        Ok(LLSDValue::Array(array))
    }

    /// Parse a map element
    fn parse_map(&self, reader: &mut Reader<&[u8]>, buf: &mut Vec<u8>) -> LLSDResult<LLSDValue> {
        let mut map = HashMap::new();
        let mut current_key: Option<String> = None;
        
        loop {
            match reader.read_event(buf) {
                Ok(Event::Start(ref e)) => {
                    let tag_name = String::from_utf8_lossy(e.name());
                    if tag_name == "key" {
                        current_key = Some(self.read_text_content(reader, buf)?);
                    } else if let Some(key) = current_key.take() {
                        let value = self.parse_typed_element(&tag_name, reader, buf)?;
                        map.insert(key, value);
                    } else {
                        return Err(LLSDError::custom("Map value without key"));
                    }
                }
                Ok(Event::Empty(ref e)) => {
                    let tag_name = String::from_utf8_lossy(e.name());
                    if let Some(key) = current_key.take() {
                        let value = self.parse_empty_element(&tag_name)?;
                        map.insert(key, value);
                    } else if tag_name != "key" {
                        return Err(LLSDError::custom("Empty map value without key"));
                    }
                }
                Ok(Event::End(ref e)) if e.name() == b"map" => break,
                Ok(Event::Eof) => break,
                Err(e) => return Err(LLSDError::from(e)),
                _ => {}
            }
        }
        
        Ok(LLSDValue::Map(map))
    }

    /// Skip to the end of an element
    fn skip_to_end(&self, reader: &mut Reader<&[u8]>, buf: &mut Vec<u8>, tag: &str) -> LLSDResult<()> {
        reader.read_to_end(tag.as_bytes(), buf)?;
        Ok(())
    }
}

/// LLSD XML serializer
#[derive(Debug)]
pub struct LLSDXmlSerializer {
    pretty_print: bool,
    indent_size: usize,
}

impl Default for LLSDXmlSerializer {
    fn default() -> Self {
        Self {
            pretty_print: false,
            indent_size: 2,
        }
    }
}

impl LLSDXmlSerializer {
    /// Create a new XML serializer
    pub fn new() -> Self {
        Self::default()
    }

    /// Enable pretty printing
    pub fn with_pretty_print(mut self, pretty: bool) -> Self {
        self.pretty_print = pretty;
        self
    }

    /// Set indent size for pretty printing
    pub fn with_indent_size(mut self, size: usize) -> Self {
        self.indent_size = size;
        self
    }

    /// Serialize LLSD to XML string
    pub fn serialize(&self, document: &LLSDDocument) -> LLSDResult<String> {
        let mut output = Vec::new();
        let mut writer = Writer::new(Cursor::new(&mut output));
        
        // Write XML declaration
        writer.write_event(Event::Decl(quick_xml::events::BytesDecl::new(
            b"1.0", Some(b"UTF-8"), None
        )))?;
        
        if self.pretty_print {
            writer.write_event(Event::Text(BytesText::from_plain_str("\n")))?;
        }

        // Write LLSD root element
        writer.write_event(Event::Start(BytesStart::borrowed_name(b"llsd")))?;
        
        if self.pretty_print {
            writer.write_event(Event::Text(BytesText::from_plain_str("\n")))?;
        }

        self.write_value(&mut writer, document.content(), if self.pretty_print { 1 } else { 0 })?;
        
        if self.pretty_print {
            writer.write_event(Event::Text(BytesText::from_plain_str("\n")))?;
        }

        writer.write_event(Event::End(BytesEnd::borrowed(b"llsd")))?;

        String::from_utf8(output).map_err(|e| LLSDError::from(e))
    }

    /// Write an LLSD value as XML
    fn write_value<W: std::io::Write>(
        &self,
        writer: &mut Writer<W>,
        value: &LLSDValue,
        depth: usize,
    ) -> LLSDResult<()> {
        let indent = if self.pretty_print {
            " ".repeat(depth * self.indent_size)
        } else {
            String::new()
        };

        if self.pretty_print && depth > 0 {
            writer.write_event(Event::Text(BytesText::from_plain_str(&indent)))?;
        }

        match value {
            LLSDValue::Undefined => {
                writer.write_event(Event::Empty(BytesStart::borrowed_name(b"undef")))?;
            }
            LLSDValue::Boolean(b) => {
                let content = if *b { "1" } else { "0" };
                writer.write_event(Event::Start(BytesStart::borrowed_name(b"boolean")))?;
                writer.write_event(Event::Text(BytesText::from_plain_str(content)))?;
                writer.write_event(Event::End(BytesEnd::borrowed(b"boolean")))?;
            }
            LLSDValue::Integer(i) => {
                writer.write_event(Event::Start(BytesStart::borrowed_name(b"integer")))?;
                writer.write_event(Event::Text(BytesText::from_plain_str(&i.to_string())))?;
                writer.write_event(Event::End(BytesEnd::borrowed(b"integer")))?;
            }
            LLSDValue::Real(r) => {
                writer.write_event(Event::Start(BytesStart::borrowed_name(b"real")))?;
                writer.write_event(Event::Text(BytesText::from_plain_str(&r.to_string())))?;
                writer.write_event(Event::End(BytesEnd::borrowed(b"real")))?;
            }
            LLSDValue::String(s) => {
                writer.write_event(Event::Start(BytesStart::borrowed_name(b"string")))?;
                writer.write_event(Event::Text(BytesText::from_escaped_str(s)))?;
                writer.write_event(Event::End(BytesEnd::borrowed(b"string")))?;
            }
            LLSDValue::UUID(u) => {
                writer.write_event(Event::Start(BytesStart::borrowed_name(b"uuid")))?;
                writer.write_event(Event::Text(BytesText::from_plain_str(&u.to_string())))?;
                writer.write_event(Event::End(BytesEnd::borrowed(b"uuid")))?;
            }
            LLSDValue::Date(d) => {
                writer.write_event(Event::Start(BytesStart::borrowed_name(b"date")))?;
                writer.write_event(Event::Text(BytesText::from_plain_str(&d.to_rfc3339())))?;
                writer.write_event(Event::End(BytesEnd::borrowed(b"date")))?;
            }
            LLSDValue::URI(u) => {
                writer.write_event(Event::Start(BytesStart::borrowed_name(b"uri")))?;
                writer.write_event(Event::Text(BytesText::from_escaped_str(u)))?;
                writer.write_event(Event::End(BytesEnd::borrowed(b"uri")))?;
            }
            LLSDValue::Binary(b) => {
                let base64_str = base64::encode(b);
                writer.write_event(Event::Start(BytesStart::borrowed_name(b"binary")))?;
                writer.write_event(Event::Text(BytesText::from_plain_str(&base64_str)))?;
                writer.write_event(Event::End(BytesEnd::borrowed(b"binary")))?;
            }
            LLSDValue::Array(arr) => {
                writer.write_event(Event::Start(BytesStart::borrowed_name(b"array")))?;
                
                for item in arr {
                    if self.pretty_print {
                        writer.write_event(Event::Text(BytesText::from_plain_str("\n")))?;
                    }
                    self.write_value(writer, item, depth + 1)?;
                }
                
                if self.pretty_print && !arr.is_empty() {
                    writer.write_event(Event::Text(BytesText::from_plain_str("\n")))?;
                    writer.write_event(Event::Text(BytesText::from_plain_str(&indent)))?;
                }
                
                writer.write_event(Event::End(BytesEnd::borrowed(b"array")))?;
            }
            LLSDValue::Map(map) => {
                writer.write_event(Event::Start(BytesStart::borrowed_name(b"map")))?;
                
                for (key, val) in map {
                    if self.pretty_print {
                        writer.write_event(Event::Text(BytesText::from_plain_str("\n")))?;
                        writer.write_event(Event::Text(BytesText::from_plain_str(
                            &" ".repeat((depth + 1) * self.indent_size)
                        )))?;
                    }
                    
                    writer.write_event(Event::Start(BytesStart::borrowed_name(b"key")))?;
                    writer.write_event(Event::Text(BytesText::from_escaped_str(key)))?;
                    writer.write_event(Event::End(BytesEnd::borrowed(b"key")))?;
                    
                    if self.pretty_print {
                        writer.write_event(Event::Text(BytesText::from_plain_str("\n")))?;
                    }
                    
                    self.write_value(writer, val, depth + 1)?;
                }
                
                if self.pretty_print && !map.is_empty() {
                    writer.write_event(Event::Text(BytesText::from_plain_str("\n")))?;
                    writer.write_event(Event::Text(BytesText::from_plain_str(&indent)))?;
                }
                
                writer.write_event(Event::End(BytesEnd::borrowed(b"map")))?;
            }
        }

        if self.pretty_print && depth > 0 {
            writer.write_event(Event::Text(BytesText::from_plain_str("\n")))?;
        }

        Ok(())
    }
}