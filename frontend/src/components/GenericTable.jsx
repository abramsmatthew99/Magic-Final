import React from 'react';
import './GenericTable.css'; // Importing the new CSS file

const GenericTable = ({ columns, data, actions }) => {
    return (
        <div className="generic-table-container shadow-sm">
            <div className="table-responsive">
                <table className="generic-table">
                    <thead>
                        <tr>
                            {columns.map((col, index) => (
                                <th key={index}>{col.header}</th>
                            ))}
                            {actions && <th className="text-end">Actions</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {data.length === 0 ? (
                            <tr>
                                <td colSpan={columns.length + (actions ? 1 : 0)} className="empty-message">
                                    No data available
                                </td>
                            </tr>
                        ) : (
                            data.map((row, rowIndex) => (
                                <tr key={row.deckId || row.id || rowIndex}>
                                    {columns.map((col, colIndex) => (
                                        <td key={colIndex}>
                                            {col.render ? col.render(row) : row[col.key]}
                                        </td>
                                    ))}
                                    
                                    {actions && (
                                        <td className="text-end action-cell">
                                            {actions(row)}
                                        </td>
                                    )}
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default GenericTable;