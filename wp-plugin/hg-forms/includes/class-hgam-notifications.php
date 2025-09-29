<?php
/**
 * HGAM Notifications API
 * Handles push notifications and notification history
 */

if (!defined('ABSPATH')) {
    exit;
}

class HGAM_Notifications {
    
    public function __construct() {
        // This class is instantiated by the main plugin
    }
    
    /**
     * Register FCM token for push notifications
     */
    public function register_token($user_id, $fcm_token, $device_id) {
        // Store FCM token in user meta
        update_user_meta($user_id, 'fcm_token', $fcm_token);
        update_user_meta($user_id, 'device_id', $device_id);
        update_user_meta($user_id, 'fcm_token_updated', current_time('mysql'));
        
        return array('success' => true, 'message' => 'Token erfolgreich registriert');
    }
    
    /**
     * Get notification history for user
     */
    public function get_notification_history($user_id, $limit = 50) {
        global $wpdb;
        $notifications_table = $wpdb->prefix . 'hgam_notifications';
        
        // Check if table exists
        if ($wpdb->get_var("SHOW TABLES LIKE '$notifications_table'") != $notifications_table) {
            $this->create_notifications_table();
        }
        
        $results = $wpdb->get_results($wpdb->prepare("
            SELECT id, title, message, type, received_at, is_read, deep_link
            FROM $notifications_table
            WHERE user_id = %d
            ORDER BY received_at DESC
            LIMIT %d
        ", $user_id, $limit));
        
        $notifications = array();
        foreach ($results as $row) {
            $notifications[] = array(
                'id' => (int) $row->id,
                'title' => $row->title,
                'message' => $row->message,
                'type' => $row->type,
                'received_at' => $row->received_at,
                'is_read' => (bool) $row->is_read,
                'deep_link' => $row->deep_link
            );
        }
        
        return $notifications;
    }
    
    /**
     * Mark notification as read
     */
    public function mark_as_read($notification_id, $user_id) {
        global $wpdb;
        $notifications_table = $wpdb->prefix . 'hgam_notifications';
        
        $result = $wpdb->update(
            $notifications_table,
            array('is_read' => 1),
            array('id' => $notification_id, 'user_id' => $user_id),
            array('%d'),
            array('%d', '%d')
        );
        
        return $result !== false;
    }
    
    /**
     * Send push notification to user
     */
    public function send_notification($user_id, $title, $message, $type = 'info', $deep_link = null) {
        // Get user's FCM token
        $fcm_token = get_user_meta($user_id, 'fcm_token', true);
        
        if (empty($fcm_token)) {
            return new WP_Error('no_token', 'Kein FCM Token für Benutzer gefunden', array('status' => 400));
        }
        
        // Store notification in database
        $this->store_notification($user_id, $title, $message, $type, $deep_link);
        
        // Send via FCM (simplified - in production use proper FCM implementation)
        $this->send_fcm_notification($fcm_token, $title, $message, $deep_link);
        
        return array('success' => true, 'message' => 'Benachrichtigung gesendet');
    }
    
    /**
     * Store notification in database
     */
    private function store_notification($user_id, $title, $message, $type, $deep_link) {
        global $wpdb;
        $notifications_table = $wpdb->prefix . 'hgam_notifications';
        
        // Check if table exists
        if ($wpdb->get_var("SHOW TABLES LIKE '$notifications_table'") != $notifications_table) {
            $this->create_notifications_table();
        }
        
        $wpdb->insert(
            $notifications_table,
            array(
                'user_id' => $user_id,
                'title' => sanitize_text_field($title),
                'message' => sanitize_text_field($message),
                'type' => sanitize_text_field($type),
                'deep_link' => sanitize_text_field($deep_link),
                'received_at' => current_time('mysql'),
                'is_read' => 0
            ),
            array('%d', '%s', '%s', '%s', '%s', '%s', '%d')
        );
    }
    
    /**
     * Send FCM notification (simplified implementation)
     */
    private function send_fcm_notification($fcm_token, $title, $message, $deep_link) {
        // In production, implement proper FCM sending
        // This is a placeholder implementation
        
        $notification_data = array(
            'to' => $fcm_token,
            'notification' => array(
                'title' => $title,
                'body' => $message,
                'click_action' => $deep_link
            ),
            'data' => array(
                'deep_link' => $deep_link ?: '',
                'type' => 'hgam_notification'
            )
        );
        
        // Log for debugging
        error_log('FCM Notification would be sent: ' . json_encode($notification_data));
        
        return true;
    }
    
    /**
     * Create notifications table
     */
    private function create_notifications_table() {
        global $wpdb;
        
        $table_name = $wpdb->prefix . 'hgam_notifications';
        
        $charset_collate = $wpdb->get_charset_collate();
        
        $sql = "CREATE TABLE $table_name (
            id mediumint(9) NOT NULL AUTO_INCREMENT,
            user_id bigint(20) NOT NULL,
            title varchar(255) NOT NULL,
            message text NOT NULL,
            type varchar(50) DEFAULT 'info',
            deep_link varchar(500),
            received_at datetime DEFAULT CURRENT_TIMESTAMP,
            is_read tinyint(1) DEFAULT 0,
            PRIMARY KEY (id),
            KEY user_id (user_id),
            KEY received_at (received_at),
            KEY is_read (is_read)
        ) $charset_collate;";
        
        require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
        dbDelta($sql);
    }
    
    /**
     * Send notification to all users with specific role
     */
    public function send_notification_to_role($role, $title, $message, $type = 'info', $deep_link = null) {
        $users = get_users(array('role' => $role));
        
        $results = array();
        foreach ($users as $user) {
            $result = $this->send_notification($user->ID, $title, $message, $type, $deep_link);
            $results[] = array(
                'user_id' => $user->ID,
                'username' => $user->user_login,
                'success' => !is_wp_error($result)
            );
        }
        
        return $results;
    }
    
    /**
     * Send notification to users in specific jagdgebiet
     */
    public function send_notification_to_jagdgebiet($jagdgebiet_id, $title, $message, $type = 'info', $deep_link = null) {
        $users = get_users(array(
            'meta_query' => array(
                array(
                    'key' => 'jagdgebiete',
                    'value' => $jagdgebiet_id,
                    'compare' => 'LIKE'
                )
            )
        ));
        
        $results = array();
        foreach ($users as $user) {
            $result = $this->send_notification($user->ID, $title, $message, $type, $deep_link);
            $results[] = array(
                'user_id' => $user->ID,
                'username' => $user->user_login,
                'success' => !is_wp_error($result)
            );
        }
        
        return $results;
    }
}
