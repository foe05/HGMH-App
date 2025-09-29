<?php
/**
 * HGAM OCR API
 * Handles Wildursprungsschein text recognition
 */

if (!defined('ABSPATH')) {
    exit;
}

class HGAM_OCR {
    
    public function __construct() {
        // This class is instantiated by the main plugin
    }
    
    /**
     * Analyze uploaded image for OCR
     */
    public function analyze_image($image_data) {
        // For now, return mock data
        // In production, integrate with actual OCR service (Google Vision API, AWS Textract, etc.)
        
        $mock_ocr_result = array(
            'wus_nummer' => $this->extract_wus_from_mock($image_data),
            'wildart' => $this->extract_wildart_from_mock($image_data),
            'datum' => $this->extract_datum_from_mock($image_data),
            'jagdgebiet' => $this->extract_jagdgebiet_from_mock($image_data),
            'erleger' => $this->extract_erleger_from_mock($image_data),
            'geschlecht' => $this->extract_geschlecht_from_mock($image_data),
            'altersklasse' => $this->extract_altersklasse_from_mock($image_data),
            'confidence' => 0.85,
            'raw_text' => $this->extract_raw_text_from_mock($image_data)
        );
        
        return $mock_ocr_result;
    }
    
    /**
     * Extract WUS number from mock data
     */
    private function extract_wus_from_mock($image_data) {
        // Mock: Generate random 7-digit WUS number
        return str_pad(rand(1000000, 9999999), 7, '0', STR_PAD_LEFT);
    }
    
    /**
     * Extract wildart from mock data
     */
    private function extract_wildart_from_mock($image_data) {
        $wildarten = array('Rotwild', 'Damwild');
        return $wildarten[array_rand($wildarten)];
    }
    
    /**
     * Extract date from mock data
     */
    private function extract_datum_from_mock($image_data) {
        // Mock: Return current date
        return current_time('d.m.Y');
    }
    
    /**
     * Extract jagdgebiet from mock data
     */
    private function extract_jagdgebiet_from_mock($image_data) {
        $jagdgebiete = array('Jagdgebiet Nord', 'Jagdgebiet Süd', 'Jagdgebiet Ost', 'Jagdgebiet West');
        return $jagdgebiete[array_rand($jagdgebiete)];
    }
    
    /**
     * Extract erleger from mock data
     */
    private function extract_erleger_from_mock($image_data) {
        $erleger = array('Max Mustermann', 'Anna Schmidt', 'Peter Müller', 'Lisa Weber');
        return $erleger[array_rand($erleger)];
    }
    
    /**
     * Extract geschlecht from mock data
     */
    private function extract_geschlecht_from_mock($image_data) {
        $geschlechter = array('männlich', 'weiblich');
        return $geschlechter[array_rand($geschlechter)];
    }
    
    /**
     * Extract altersklasse from mock data
     */
    private function extract_altersklasse_from_mock($image_data) {
        $altersklassen = array('0', '1', '2', '3', '4');
        return $altersklassen[array_rand($altersklassen)];
    }
    
    /**
     * Extract raw text from mock data
     */
    private function extract_raw_text_from_mock($image_data) {
        return "Wildursprungsschein\nWildmarkennummer: " . $this->extract_wus_from_mock($image_data) . 
               "\nWildart: " . $this->extract_wildart_from_mock($image_data) . 
               "\nErlegungsdatum: " . $this->extract_datum_from_mock($image_data) . 
               "\nErleger: " . $this->extract_erleger_from_mock($image_data);
    }
    
    /**
     * Process OCR result and map to app format
     */
    public function process_ocr_result($ocr_data) {
        $wildart_code = $this->map_wildart_name_to_code($ocr_data['wildart']);
        $kategorie = $this->determine_kategorie($ocr_data['geschlecht'], $ocr_data['altersklasse']);
        
        return array(
            'wus_nummer' => $ocr_data['wus_nummer'],
            'wildart' => $ocr_data['wildart'],
            'wildart_code' => $wildart_code,
            'kategorie' => $kategorie,
            'datum' => $ocr_data['datum'],
            'jagdgebiet' => $ocr_data['jagdgebiet'],
            'erleger' => $ocr_data['erleger'],
            'geschlecht' => $ocr_data['geschlecht'],
            'altersklasse' => $ocr_data['altersklasse'],
            'confidence' => $ocr_data['confidence'],
            'raw_text' => $ocr_data['raw_text']
        );
    }
    
    /**
     * Map wildart name to code
     */
    private function map_wildart_name_to_code($wildart_name) {
        $mapping = array(
            'Rotwild' => 'RW',
            'Damwild' => 'DW',
            'Rehwild' => 'RW',
            'Schwarzwild' => 'SW'
        );
        
        return $mapping[$wildart_name] ?? 'UN';
    }
    
    /**
     * Determine kategorie based on geschlecht and altersklasse
     */
    private function determine_kategorie($geschlecht, $altersklasse) {
        $kategorien = array(
            'weiblich' => array(
                '0' => 'Wildkalb',
                '1' => 'Schmaltier',
                '2' => 'Alttier'
            ),
            'männlich' => array(
                '0' => 'Hirschkalb',
                '1' => 'Schmalspießer',
                '2' => 'Junger Hirsch',
                '3' => 'Mittelalter Hirsch',
                '4' => 'Alter Hirsch'
            )
        );
        
        return $kategorien[$geschlecht][$altersklasse] ?? 'Unbekannt';
    }
}
