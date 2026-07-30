/**
 * API client for FleetOps Frontend
 * Provides a clean wrapper around fetch() with standard error handling
 */

const Api = {
    // Base URL for the API
    baseUrl: '/api',

    /**
     * Centralized response handler
     */
    async handleResponse(response) {
        if (response.status === 401) {
            window.location.href = '/login?expired=true';
            return null;
        }

        if (!response.ok) {
            let errorMessage = 'An unexpected error occurred';
            try {
                const errorData = await response.json();
                if (errorData.errors) {
                    // Extract validation errors
                    errorMessage = Object.values(errorData.errors).join('\n');
                } else if (errorData.message) {
                    // Extract general error message
                    errorMessage = errorData.message;
                }
            } catch (e) {
                // If response is not JSON
                errorMessage = response.statusText;
            }
            throw new Error(errorMessage);
        }
        
        // Handle 204 No Content (DELETE requests)
        if (response.status === 204) {
            return null;
        }

        return await response.json();
    },

    /**
     * Fetch wrapper with timeout
     */
    async fetchWithTimeout(url, options = {}, timeoutMs = 10000) {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
        options.credentials = 'same-origin';
        
        try {
            const response = await fetch(url, { ...options, signal: controller.signal });
            clearTimeout(timeoutId);
            return response;
        } catch (error) {
            clearTimeout(timeoutId);
            if (error.name === 'AbortError') {
                throw new Error('Network request timed out. Please try again.');
            }
            throw new Error('Network connection failed. Please check your internet.');
        }
    },

    /**
     * GET request
     */
    async get(endpoint, params = {}) {
        const url = new URL(this.baseUrl + endpoint, window.location.origin);
        
        Object.keys(params).forEach(key => {
            if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                url.searchParams.append(key, params[key]);
            }
        });

        const response = await this.fetchWithTimeout(url, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });
        
        return this.handleResponse(response);
    },

    /**
     * POST request
     */
    async post(endpoint, data) {
        const response = await this.fetchWithTimeout(this.baseUrl + endpoint, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(data)
        });
        return this.handleResponse(response);
    },

    /**
     * PUT request
     */
    async put(endpoint, data) {
        const response = await this.fetchWithTimeout(this.baseUrl + endpoint, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(data)
        });
        return this.handleResponse(response);
    },

    /**
     * DELETE request
     */
    async delete(endpoint) {
        const response = await this.fetchWithTimeout(this.baseUrl + endpoint, {
            method: 'DELETE',
            headers: { 'Accept': 'application/json' }
        });
        return this.handleResponse(response);
    }
};
