/**
 * Utility class to parse a user's free-text query string into a structured
 * object compatible with the backend's multi-parameter search API.
 * * Syntax follows a keyword:value pattern (e.g., 'o:flying')
 */

const SYNTAX_MAP = {
    'r': 'rarity',       
    's': 'setCode',      
    'o': 'oracleText',   
    't': 'typeLine',     
    'cmc': 'cmc',       
};

/**
 * Parses a raw search query string into a structured object for the CardService API.
 * * @param {string} rawQuery The full string entered by the user (e.g., "dragon o:flying s:dmu")
 * @returns {object} An object containing structured parameters (e.g., {name: "dragon", oracleText: "flying"})
 */
export function parseQuery(rawQuery) {
    const result = {
        name: null,
        oracleText: null,
        rarity: null,
        setCode: null,
        cmc: null,
        typeLine: null
    };

    if (!rawQuery) {
        return result;
    }

    const tokens = rawQuery.trim().match(/(?:[^\s"]+|"[^"]*")+/g) || []; //Regex is so unreadable I apologize to all who read this line
    const remainingNameTokens = []; //but basically split on spaces to find different queries, except we have to find double quotes for queries that include a space themselves

    tokens.forEach(token => {
        if (token.includes(':')) {
            const [keyword, value] = token.split(':').map(s => s.trim());
            const apiParam = SYNTAX_MAP[keyword.toLowerCase()];

            if (apiParam && value) {
                // cmc is a number moment
                if (apiParam === 'cmc') {                    
                    const parsedCmc = parseInt(value);
                    result[apiParam] = isFinite(parsedCmc) ? parsedCmc : null;
                } else {
                    result[apiParam] = value;
                }
            } else {
                // If invalid keyword, treat as part of the card name search
                remainingNameTokens.push(token);
            }
        } else {
            // No colon implies they're searching for a name (or about to learn what happens without a colon)
            remainingNameTokens.push(token);
        }
    });

    
    if (remainingNameTokens.length > 0) {
        result.name = remainingNameTokens.join(' ');
    }

    return result;
}

/**
 * Returns a documentation map of supported search tags for a UI help page.
 */
export function getSyntaxHelp() {
    return [
        { tag: "r:", description: "Rarity (e.g., r:mythic, r:common)" },
        { tag: "s:", description: "Set Code (e.g., s:khm, s:cmd)" },
        { tag: "o:", description: "Oracle/Rules Text (e.g., o:haste, o:scry)" },
        { tag: "t:", description: "Type Line (e.g., t:creature, t:instant)" },
        { tag: "cmc:", description: "Converted Mana Cost / Mana Value (e.g., cmc:3)" },
    ];
}