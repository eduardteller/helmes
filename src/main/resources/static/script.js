document.addEventListener('DOMContentLoaded', function() {
    fetchSectors();
});

function fetchSectors() {
    fetch('/api/get-sectors')
        .then(response => {
            if (!response.ok) {
                throw new Error('fetchSectors error');
            }
            return response.json();
        })
        .then(sectors => {
            populateSectorsDropdown(sectors);
        })
        .catch(error => {
            console.error('error:', error);
        });
}

function populateSectorsDropdown(sectors, level = 0) {
    const selectElement = document.getElementById('sectors');

    // Clear existing options if at the top level
    if (level === 0) {
        selectElement.innerHTML = '';
    }

    sectors.forEach(sector => {
        const option = document.createElement('option');
        option.value = sector.id;

        let indent = '';
        for (let i = 0; i < level; i++) {
            indent += '&nbsp;&nbsp;&nbsp;&nbsp;';
        }

        option.innerHTML = indent + sector.name;
        selectElement.appendChild(option);

        if (sector.children && sector.children.length > 0) {
            populateSectorsDropdown(sector.children, level + 1);
        }
    });
}
