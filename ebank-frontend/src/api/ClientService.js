// src/api/ClientService.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/clients';

// Fonction utilitaire pour obtenir les headers d'autorisation JWT
const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    if (!token) {
        throw new Error("Token non trouvé. Veuillez vous reconnecter.");
    }
    return {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    };
};
// Suite de src/api/ClientService.js

/**
 * UC-4: Récupère les détails du compte sélectionné (RIB, Solde, Opérations paginées).
 * @param {number} accountId ID du compte à consulter (optionnel).
 * @param {number} page Numéro de page.
 * @returns {Promise<AccountDetailsDTO>}
 */
export const fetchDashboardData = async (accountId = null, page = 0, size = 10) => {
    try {
        const params = {
            page: page,
            size: size
        };
        if (accountId !== null) {
            params.accountId = accountId;
        }

        const response = await axios.get(
            `${API_BASE_URL}/dashboard`, 
            { 
                ...getAuthHeaders(), // JWT
                params: params      // Pagination et sélection de compte
            }
        );
        return response.data;
    } catch (error) {
        throw error;
    }
};

/**
 * UC-4: Récupère la liste de tous les comptes du client (pour la liste déroulante).
 * @returns {Promise<Array<BankAccount>>}
 */
export const fetchClientAccounts = async () => {
    try {
        console.log('Fetching accounts from:', `${API_BASE_URL}/accounts`);
        const token = localStorage.getItem('token');
        console.log('Current token:', token ? 'Token exists' : 'No token found');
        
        const headers = getAuthHeaders();
        console.log('Request headers:', headers);
        
        const response = await axios.get(
            `${API_BASE_URL}/accounts`,
            {
                ...headers,
                transformResponse: [function (data) {
                    // If the response is already an object, return it as is
                    if (typeof data === 'object') return data;
                    // Otherwise try to parse it as JSON
                    try {
                        // Handle circular references in the JSON data
                        const seen = new WeakSet();
                        return JSON.parse(data, (key, value) => {
                            if (typeof value === 'object' && value !== null) {
                                if (seen.has(value)) {
                                    return; // Remove circular references
                                }
                                seen.add(value);
                            }
                            return value;
                        });
                    } catch (e) {
                        console.error('Failed to parse response as JSON:', data.substring(0, 200) + '...');
                        return [];
                    }
                }]
            }
        );
        
        console.log('Accounts API response:', {
            status: response.status,
            statusText: response.statusText,
            headers: response.headers,
            data: response.data
        });
        
        // Handle both array and object responses
        let accounts = [];
        if (Array.isArray(response.data)) {
            accounts = response.data;
        } else if (response.data && Array.isArray(response.data.accounts)) {
            accounts = response.data.accounts;
        } else if (response.data) {
            // If it's a single account, wrap it in an array
            accounts = [response.data];
        }
        console.log('Returning accounts:', accounts);
        return accounts;
    } catch (error) {
        console.error('Error in fetchClientAccounts:', {
            message: error.message,
            response: error.response ? {
                status: error.response.status,
                statusText: error.response.statusText,
                data: error.response.data,
                headers: error.response.headers
            } : 'No response',
            request: error.request ? 'Request was made but no response received' : 'No request was made',
            config: {
                url: error.config?.url,
                method: error.config?.method,
                headers: error.config?.headers
            }
        });
        throw error;
    }
};
// Suite de src/api/ClientService.js

/**
 * UC-5: Effectue un nouveau virement.
 * @param {object} virementData Les données du virement (sourceRib, destinationRib, amount, reason).
 * @returns {Promise<string>} Message de succès du Backend.
 */
export const makeTransfer = async (virementData) => {
    try {
        console.log('Making transfer with data:', virementData);
        const token = localStorage.getItem('token');
        console.log('Using token:', token ? 'Token exists' : 'No token found');
        
        const response = await axios.post(
            `${API_BASE_URL}/virement`,
            virementData,
            {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            }
        );
        return response.data;
    } catch (error) {
        console.error('Transfer error:', {
            status: error.response?.status,
            data: error.response?.data,
            headers: error.response?.headers
        });
        throw error;
    }
};