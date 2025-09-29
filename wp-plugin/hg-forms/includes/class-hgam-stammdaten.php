<?php
/**
 * HGAM Stammdaten API
 * Handles wildarten, kategorien, and jagdgebiete endpoints
 */

if (!defined('ABSPATH')) {
    exit;
}

class HGAM_Stammdaten {
    
    public function __construct() {
        // This class is instantiated by the main plugin
    }
    
    /**
     * Get all wildarten (wildlife species)
     */
    public function get_wildarten() {
        global $wpdb;
        
        // Try to get from HGAM plugin tables first
        $wildarten_table = $wpdb->prefix . 'ahgmh_wildart';
        if ($wpdb->get_var("SHOW TABLES LIKE '$wildarten_table'") == $wildarten_table) {
            $results = $wpdb->get_results("
                SELECT id, name, code, meldegruppen 
                FROM $wildarten_table 
                WHERE active = 1 
                ORDER BY name ASC
            ");
            
            $wildarten = array();
            foreach ($results as $row) {
                $meldegruppen = !empty($row->meldegruppen) ? 
                    explode(',', $row->meldegruppen) : array();
                
                $wildarten[] = array(
                    'id' => (int) $row->id,
                    'name' => $row->name,
                    'code' => $row->code,
                    'meldegruppen' => $meldegruppen
                );
            }
            
            return $wildarten;
        }
        
        // Fallback: Default wildarten
        return array(
            array(
                'id' => 1,
                'name' => 'Rotwild',
                'code' => 'RW',
                'meldegruppen' => array('Gruppe_A', 'Gruppe_B')
            ),
            array(
                'id' => 2,
                'name' => 'Damwild',
                'code' => 'DW',
                'meldegruppen' => array('Gruppe_A', 'Gruppe_B')
            )
        );
    }
    
    /**
     * Get all kategorien (gender + age class combinations)
     */
    public function get_kategorien() {
        return array(
            // Female categories
            array(
                'id' => 1,
                'name' => 'Wildkalb',
                'code' => 'W0',
                'description' => 'Weiblich + Altersklasse 0'
            ),
            array(
                'id' => 2,
                'name' => 'Schmaltier',
                'code' => 'W1',
                'description' => 'Weiblich + Altersklasse 1'
            ),
            array(
                'id' => 3,
                'name' => 'Alttier',
                'code' => 'W2',
                'description' => 'Weiblich + Altersklasse 2'
            ),
            // Male categories
            array(
                'id' => 4,
                'name' => 'Hirschkalb',
                'code' => 'M0',
                'description' => 'Männlich + Altersklasse 0'
            ),
            array(
                'id' => 5,
                'name' => 'Schmalspießer',
                'code' => 'M1',
                'description' => 'Männlich + Altersklasse 1'
            ),
            array(
                'id' => 6,
                'name' => 'Junger Hirsch',
                'code' => 'M2',
                'description' => 'Männlich + Altersklasse 2'
            ),
            array(
                'id' => 7,
                'name' => 'Mittelalter Hirsch',
                'code' => 'M3',
                'description' => 'Männlich + Altersklasse 3'
            ),
            array(
                'id' => 8,
                'name' => 'Alter Hirsch',
                'code' => 'M4',
                'description' => 'Männlich + Altersklasse 4'
            )
        );
    }
    
    /**
     * Get jagdgebiete (hunting areas) based on user permissions
     */
    public function get_jagdgebiete($user_id = null) {
        if (!$user_id) {
            $user_id = get_current_user_id();
        }
        
        // Try to get from HGAM plugin tables first
        global $wpdb;
        $jagdgebiete_table = $wpdb->prefix . 'ahgmh_jagdgebiet';
        
        if ($wpdb->get_var("SHOW TABLES LIKE '$jagdgebiete_table'") == $jagdgebiete_table) {
            // Get user's assigned jagdgebiete
            $user_jagdgebiete = get_user_meta($user_id, 'jagdgebiete', true);
            
            if (empty($user_jagdgebiete)) {
                // If no specific jagdgebiete assigned, return all
                $results = $wpdb->get_results("
                    SELECT id, name, code 
                    FROM $jagdgebiete_table 
                    WHERE active = 1 
                    ORDER BY name ASC
                ");
            } else {
                // Return only assigned jagdgebiete
                $placeholders = implode(',', array_fill(0, count($user_jagdgebiete), '%d'));
                $results = $wpdb->get_results($wpdb->prepare("
                    SELECT id, name, code 
                    FROM $jagdgebiete_table 
                    WHERE active = 1 AND id IN ($placeholders)
                    ORDER BY name ASC
                ", $user_jagdgebiete));
            }
            
            $jagdgebiete = array();
            foreach ($results as $row) {
                $jagdgebiete[] = array(
                    'id' => (int) $row->id,
                    'name' => $row->name,
                    'code' => $row->code
                );
            }
            
            return $jagdgebiete;
        }
        
        // Fallback: Default jagdgebiete
        return array(
            array(
                'id' => 1,
                'name' => 'Jagdgebiet Nord',
                'code' => 'JG_N'
            ),
            array(
                'id' => 2,
                'name' => 'Jagdgebiet Süd',
                'code' => 'JG_S'
            ),
            array(
                'id' => 3,
                'name' => 'Jagdgebiet Ost',
                'code' => 'JG_O'
            ),
            array(
                'id' => 4,
                'name' => 'Jagdgebiet West',
                'code' => 'JG_W'
            )
        );
    }
    
    /**
     * Validate WUS number (7 digits, unique)
     */
    public function validate_wus_number($wus_number, $exclude_id = null) {
        // Check format (7 digits)
        if (!preg_match('/^\d{7}$/', $wus_number)) {
            return array(
                'valid' => false,
                'message' => 'WUS-Nummer muss 7-stellig sein und nur Zahlen enthalten'
            );
        }
        
        // Check uniqueness
        global $wpdb;
        $erfassungen_table = $wpdb->prefix . 'ahgmh_erfassungen';
        
        if ($wpdb->get_var("SHOW TABLES LIKE '$erfassungen_table'") == $erfassungen_table) {
            $query = "SELECT id FROM $erfassungen_table WHERE wus_nummer = %s";
            $params = array($wus_number);
            
            if ($exclude_id) {
                $query .= " AND id != %d";
                $params[] = $exclude_id;
            }
            
            $existing = $wpdb->get_var($wpdb->prepare($query, $params));
            
            if ($existing) {
                return array(
                    'valid' => false,
                    'message' => 'WUS-Nummer bereits vergeben'
                );
            }
        }
        
        return array(
            'valid' => true,
            'message' => 'WUS-Nummer ist gültig'
        );
    }
}
