// Recording functionality variables
let isRecording = false;
let mediaRecorder = null;
let recordedChunks = [];
let videoPlayer = null;

// Chart variables
let viewerChart, performanceChart, interactionsChart, retentionChart, ageChart, deviceChart, loyaltyChart;

// WebSocket client
let stompClient = null;
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;

// Highlights functionality variables
let currentStreamId = 1; // Default stream ID
let highlights = [];
let currentFilter = 'all';
let currentHighlightId = null;

// Quality management variables
let currentQuality = 'auto';
let isQualityRecording = false;
let qualityRecordingStartTime = null;
let qualityRecordingInterval = null;
let currentBandwidth = 0;
let qualitySwitchHistory = [];
let currentRecordingId = null;
let userQualityPreference = {
    preferredQuality: 'auto',
    autoSwitch: true,
    bandwidthThreshold: 5000
};

// User ID for quality management
let userId = 'user_' + Math.random().toString(36).substr(2, 9);

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing application...');
    
    // Initialize video.js player
    videoPlayer = videojs('videoPlayer', {
        controls: true,
        autoplay: true,
        preload: 'auto'
    });

    // Initialize recording button
    const recordButton = document.getElementById('recordButton');
    if (recordButton) {
        recordButton.addEventListener('click', function() {
            console.log('Record button clicked');
            if (!isRecording) {
                startRecording();
            } else {
                stopRecording();
            }
        });
    }

    // Initialize like/dislike buttons
    const likeButton = document.getElementById('likeButton');
    const dislikeButton = document.getElementById('dislikeButton');
    
    if (likeButton) {
        likeButton.addEventListener('click', function() {
            console.log('Like button clicked');
            incrementLike();
        });
    }
    
    if (dislikeButton) {
        dislikeButton.addEventListener('click', function() {
            console.log('Dislike button clicked');
            incrementDislike();
        });
    }

    // Initialize highlights functionality
    initializeHighlights();

    // Initialize quality management
    initializeQualityManagement();

    // Initialize charts
    initializeCharts();
    
    // Initialize chat functionality
    initializeChat();

    // Initialize WebSocket connection
    connectWebSocket();

    // Fetch and display recordings
    fetchRecordings();

    // Initialize character count for chat
    initializeChatCharacterCount();

    // Initialize clear chat and refresh buttons
    initializeChatControls();

    // Existing tab functionality
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    function initializeTabs() {
        tabButtons.forEach(button => {
            button.classList.remove('active-tab');
        });

        tabContents.forEach(content => {
            content.classList.add('hidden');
        });
    }

    function showTabContent(tabId) {
        const activeButton = document.querySelector(`[data-tab="${tabId}"]`);
        activeButton.classList.add('active-tab');

        const activeContent = document.getElementById(tabId);
        activeContent.classList.remove('hidden');
    }

    initializeTabs();
    showTabContent('realtime');

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.dataset.tab;
            initializeTabs();
            showTabContent(tabId);
        });
    });
});

// Highlights functionality
function initializeHighlights() {
    console.log('Initializing highlights functionality...');
    
    // Create highlight button
    const createHighlightBtn = document.getElementById('createHighlightBtn');
    if (createHighlightBtn) {
        createHighlightBtn.addEventListener('click', showCreateHighlightModal);
    }

    // Modal close buttons
    const closeHighlightModal = document.getElementById('closeHighlightModal');
    const cancelHighlightBtn = document.getElementById('cancelHighlightBtn');
    const closeDetailModal = document.getElementById('closeDetailModal');
    
    if (closeHighlightModal) {
        closeHighlightModal.addEventListener('click', hideCreateHighlightModal);
    }
    
    if (cancelHighlightBtn) {
        cancelHighlightBtn.addEventListener('click', hideCreateHighlightModal);
    }
    
    if (closeDetailModal) {
        closeDetailModal.addEventListener('click', hideHighlightDetailModal);
    }

    // Capture time button
    const captureTimeBtn = document.getElementById('captureTimeBtn');
    if (captureTimeBtn) {
        captureTimeBtn.addEventListener('click', captureCurrentTime);
    }

    // Highlight form submission
    const highlightForm = document.getElementById('highlightForm');
    if (highlightForm) {
        highlightForm.addEventListener('submit', createHighlight);
    }

    // Filter buttons
    const filterButtons = document.querySelectorAll('.filter-btn');
    filterButtons.forEach(button => {
        button.addEventListener('click', function() {
            const filter = this.dataset.filter;
            setFilter(filter);
        });
    });

    // Search functionality
    const searchHighlightsBtn = document.getElementById('searchHighlightsBtn');
    const highlightSearch = document.getElementById('highlightSearch');
    
    if (searchHighlightsBtn) {
        searchHighlightsBtn.addEventListener('click', searchHighlights);
    }
    
    if (highlightSearch) {
        highlightSearch.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchHighlights();
            }
        });
    }

    // Load initial highlights
    loadHighlights();
    
    // Update current time display
    updateCurrentTimeDisplay();
    setInterval(updateCurrentTimeDisplay, 1000);
}

function showCreateHighlightModal() {
    console.log('Showing create highlight modal');
    const modal = document.getElementById('highlightModal');
    if (modal) {
        modal.classList.remove('hidden');
        updateCurrentTimeDisplay();
    }
}

function hideCreateHighlightModal() {
    console.log('Hiding create highlight modal');
    const modal = document.getElementById('highlightModal');
    if (modal) {
        modal.classList.add('hidden');
        // Reset form
        const form = document.getElementById('highlightForm');
        if (form) {
            form.reset();
        }
    }
}

function hideHighlightDetailModal() {
    console.log('Hiding highlight detail modal');
    const modal = document.getElementById('highlightDetailModal');
    if (modal) {
        modal.classList.add('hidden');
    }
}

function updateCurrentTimeDisplay() {
    const currentTimeElement = document.getElementById('currentTime');
    if (currentTimeElement && videoPlayer) {
        const currentTime = Math.floor(videoPlayer.currentTime());
        const minutes = Math.floor(currentTime / 60);
        const seconds = currentTime % 60;
        currentTimeElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }
}

function captureCurrentTime() {
    console.log('Capturing current time');
    updateCurrentTimeDisplay();
}

function createHighlight(event) {
    event.preventDefault();
    console.log('Creating highlight...');
    
    const title = document.getElementById('highlightTitle').value;
    const description = document.getElementById('highlightDescription').value;
    const type = document.getElementById('highlightType').value;
    const tagsInput = document.getElementById('highlightTags').value;
    const currentTimeElement = document.getElementById('currentTime');
    
    if (!title.trim()) {
        alert('Please enter a title for the highlight');
        return;
    }
    
    // Calculate timestamp in seconds
    let timestampSeconds = 0;
    if (currentTimeElement && videoPlayer) {
        timestampSeconds = Math.floor(videoPlayer.currentTime());
    }
    
    // Parse tags
    const tags = tagsInput.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0);
    
    const highlightData = {
        streamId: currentStreamId,
        title: title,
        description: description,
        timestampSeconds: timestampSeconds,
        createdBy: 'User', // In a real app, this would be the logged-in user
        type: type,
        tags: tags
    };
    
    console.log('Highlight data:', highlightData);
    
    fetch('/api/highlights/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(highlightData)
    })
    .then(response => response.json())
    .then(data => {
        console.log('Highlight created:', data);
        if (data.success) {
            hideCreateHighlightModal();
            loadHighlights(); // Refresh the highlights list
            showNotification('Highlight created successfully!', 'success');
        } else {
            showNotification('Error creating highlight: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error creating highlight:', error);
        showNotification('Error creating highlight', 'error');
    });
}

function loadHighlights() {
    console.log('Loading highlights for stream:', currentStreamId);
    
    const container = document.getElementById('highlightsContainer');
    if (container) {
        container.innerHTML = '<div class="highlight-loading"><i class="fas fa-spinner"></i> Loading highlights...</div>';
    }
    
    fetch(`/api/highlights/stream/${currentStreamId}`)
        .then(response => response.json())
        .then(data => {
            console.log('Highlights loaded:', data);
            if (data.success) {
                highlights = data.highlights;
                renderHighlights();
            } else {
                showNotification('Error loading highlights: ' + data.message, 'error');
                renderEmptyState();
            }
        })
        .catch(error => {
            console.error('Error loading highlights:', error);
            showNotification('Error loading highlights', 'error');
            renderEmptyState();
        });
}

function renderHighlights() {
    const container = document.getElementById('highlightsContainer');
    if (!container) return;
    
    if (highlights.length === 0) {
        renderEmptyState();
        return;
    }
    
    // Filter highlights based on current filter
    let filteredHighlights = highlights;
    if (currentFilter !== 'all') {
        filteredHighlights = highlights.filter(h => h.highlightType === currentFilter);
    }
    
    if (filteredHighlights.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-star"></i>
                <h3>No ${currentFilter.toLowerCase()}s found</h3>
                <p>Create your first ${currentFilter.toLowerCase()} to get started!</p>
            </div>
        `;
        return;
    }
    
    const highlightsHTML = filteredHighlights.map(highlight => createHighlightCard(highlight)).join('');
    container.innerHTML = highlightsHTML;
    
    // Add event listeners to highlight cards
    addHighlightCardListeners();
}

function createHighlightCard(highlight) {
    const tagsHTML = highlight.tags && highlight.tags.length > 0 
        ? highlight.tags.map(tag => `<span class="highlight-tag">${tag}</span>`).join('')
        : '';
    
    return `
        <div class="highlight-card ${highlight.highlightType.toLowerCase()}" data-highlight-id="${highlight.highlightId}">
            <div class="highlight-header">
                <div class="flex-1">
                    <div class="highlight-title">${highlight.title}</div>
                    <div class="highlight-time">${highlight.timestampFormatted}</div>
                </div>
                <div class="highlight-actions">
                    <button class="highlight-action-btn jump-btn" title="Jump to time">
                        <i class="fas fa-play"></i>
                    </button>
                    <button class="highlight-action-btn like-btn" title="Like">
                        <i class="fas fa-thumbs-up"></i>
                        <span class="like-count">${highlight.likeCount || 0}</span>
                    </button>
                    <button class="highlight-action-btn comment-btn" title="Comments">
                        <i class="fas fa-comment"></i>
                        <span class="comment-count">${highlight.commentCount || 0}</span>
                    </button>
                    <button class="highlight-action-btn detail-btn" title="View details">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>
            </div>
            ${highlight.description ? `<div class="highlight-description">${highlight.description}</div>` : ''}
            ${tagsHTML ? `<div class="highlight-tags">${tagsHTML}</div>` : ''}
            <div class="highlight-meta">
                <span>By ${highlight.createdBy}</span>
                <span>${formatDate(highlight.createdAt)}</span>
            </div>
        </div>
    `;
}

function addHighlightCardListeners() {
    // Jump to time button
    document.querySelectorAll('.jump-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const highlightId = this.closest('.highlight-card').dataset.highlightId;
            jumpToHighlight(highlightId);
        });
    });
    
    // Like button
    document.querySelectorAll('.like-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const highlightId = this.closest('.highlight-card').dataset.highlightId;
            likeHighlight(highlightId);
        });
    });
    
    // Comment button
    document.querySelectorAll('.comment-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const highlightId = this.closest('.highlight-card').dataset.highlightId;
            showHighlightComments(highlightId);
        });
    });
    
    // Detail button
    document.querySelectorAll('.detail-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const highlightId = this.closest('.highlight-card').dataset.highlightId;
            showHighlightDetails(highlightId);
        });
    });
    
    // Card click for details
    document.querySelectorAll('.highlight-card').forEach(card => {
        card.addEventListener('click', function() {
            const highlightId = this.dataset.highlightId;
            showHighlightDetails(highlightId);
        });
    });
}

function jumpToHighlight(highlightId) {
    console.log('Jumping to highlight:', highlightId);
    
    fetch(`/api/highlights/${highlightId}/timestamp`)
        .then(response => response.json())
        .then(data => {
            if (data.success && videoPlayer) {
                const timestamp = data.timestamp.timestampSeconds;
                videoPlayer.currentTime(timestamp);
                showNotification(`Jumped to ${data.timestamp.timestampFormatted}`, 'success');
            } else {
                showNotification('Error jumping to highlight', 'error');
            }
        })
        .catch(error => {
            console.error('Error jumping to highlight:', error);
            showNotification('Error jumping to highlight', 'error');
        });
}

function likeHighlight(highlightId) {
    console.log('Liking highlight:', highlightId);
    
    fetch(`/api/highlights/${highlightId}/reaction`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            reactionType: 'LIKE'
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Update the like count in the UI
            const likeBtn = document.querySelector(`[data-highlight-id="${highlightId}"] .like-btn`);
            if (likeBtn) {
                const likeCount = likeBtn.querySelector('.like-count');
                const currentCount = parseInt(likeCount.textContent);
                likeCount.textContent = currentCount + 1;
                likeBtn.classList.add('liked');
            }
            showNotification('Highlight liked!', 'success');
        } else {
            showNotification('Error liking highlight: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error liking highlight:', error);
        showNotification('Error liking highlight', 'error');
    });
}

function showHighlightComments(highlightId) {
    console.log('Showing comments for highlight:', highlightId);
    
    fetch(`/api/highlights/${highlightId}/comments`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showCommentsModal(highlightId, data.comments);
            } else {
                showNotification('Error loading comments: ' + data.message, 'error');
            }
        })
        .catch(error => {
            console.error('Error loading comments:', error);
            showNotification('Error loading comments', 'error');
        });
}

function showCommentsModal(highlightId, comments) {
    const modal = document.getElementById('highlightDetailModal');
    const title = document.getElementById('detailTitle');
    const content = document.getElementById('highlightDetailContent');
    
    if (modal && title && content) {
        title.textContent = 'Comments';
        
        const commentsHTML = comments.length > 0 
            ? comments.map(comment => `
                <div class="comment">
                    <div class="comment-header">
                        <span class="comment-author">${comment.username}</span>
                        <span class="comment-time">${formatDate(comment.createdAt)}</span>
                    </div>
                    <div class="comment-content">${comment.content}</div>
                </div>
            `).join('')
            : '<p class="text-gray-500 text-center py-4">No comments yet. Be the first to comment!</p>';
        
        content.innerHTML = `
            <div class="comment-section">
                ${commentsHTML}
                <div class="mt-4">
                    <textarea id="newComment" placeholder="Add a comment..." class="form-input form-textarea"></textarea>
                    <button onclick="addComment(${highlightId})" class="btn-primary mt-2">Add Comment</button>
                </div>
            </div>
        `;
        
        modal.classList.remove('hidden');
    }
}

function addComment(highlightId) {
    const commentInput = document.getElementById('newComment');
    const content = commentInput.value.trim();
    
    if (!content) {
        showNotification('Please enter a comment', 'error');
        return;
    }
    
    fetch(`/api/highlights/${highlightId}/comment`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            username: 'User', // In a real app, this would be the logged-in user
            content: content
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            commentInput.value = '';
            showHighlightComments(highlightId); // Refresh comments
            showNotification('Comment added successfully!', 'success');
        } else {
            showNotification('Error adding comment: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error adding comment:', error);
        showNotification('Error adding comment', 'error');
    });
}

function showHighlightDetails(highlightId) {
    console.log('Showing details for highlight:', highlightId);
    
    const highlight = highlights.find(h => h.highlightId == highlightId);
    if (!highlight) {
        showNotification('Highlight not found', 'error');
        return;
    }
    
    const modal = document.getElementById('highlightDetailModal');
    const title = document.getElementById('detailTitle');
    const content = document.getElementById('highlightDetailContent');
    
    if (modal && title && content) {
        title.textContent = highlight.title;
        
        const tagsHTML = highlight.tags && highlight.tags.length > 0 
            ? highlight.tags.map(tag => `<span class="highlight-tag">${tag}</span>`).join('')
            : '<span class="text-gray-500">No tags</span>';
        
        content.innerHTML = `
            <div class="mb-4">
                <div class="flex items-center justify-between mb-2">
                    <span class="highlight-time">${highlight.timestampFormatted}</span>
                    <span class="text-sm text-gray-500">${highlight.highlightType}</span>
                </div>
                ${highlight.description ? `<p class="mb-4">${highlight.description}</p>` : ''}
                <div class="mb-4">
                    <strong>Tags:</strong>
                    <div class="highlight-tags mt-1">${tagsHTML}</div>
                </div>
                <div class="flex items-center justify-between text-sm text-gray-500">
                    <span>Created by ${highlight.createdBy}</span>
                    <span>${formatDate(highlight.createdAt)}</span>
                </div>
            </div>
            <div class="flex space-x-2">
                <button onclick="jumpToHighlight(${highlightId})" class="btn-primary">
                    <i class="fas fa-play mr-2"></i>Jump to Time
                </button>
                <button onclick="likeHighlight(${highlightId})" class="btn-secondary">
                    <i class="fas fa-thumbs-up mr-2"></i>Like (${highlight.likeCount || 0})
                </button>
                <button onclick="showHighlightComments(${highlightId})" class="btn-secondary">
                    <i class="fas fa-comment mr-2"></i>Comments (${highlight.commentCount || 0})
                </button>
            </div>
        `;
        
        modal.classList.remove('hidden');
    }
}

function setFilter(filter) {
    console.log('Setting filter:', filter);
    currentFilter = filter;
    
    // Update filter button states
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-filter="${filter}"]`).classList.add('active');
    
    // Re-render highlights
    renderHighlights();
}

function searchHighlights() {
    const searchTerm = document.getElementById('highlightSearch').value.trim();
    console.log('Searching highlights:', searchTerm);
    
    if (!searchTerm) {
        loadHighlights();
        return;
    }
    
    // For now, we'll do client-side search
    // In a real app, you'd send this to the server
    const filteredHighlights = highlights.filter(highlight => 
        highlight.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        highlight.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (highlight.tags && highlight.tags.some(tag => tag.toLowerCase().includes(searchTerm.toLowerCase())))
    );
    
    const container = document.getElementById('highlightsContainer');
    if (container) {
        if (filteredHighlights.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-search"></i>
                    <h3>No highlights found</h3>
                    <p>Try a different search term</p>
                </div>
            `;
        } else {
            const highlightsHTML = filteredHighlights.map(highlight => createHighlightCard(highlight)).join('');
            container.innerHTML = highlightsHTML;
            addHighlightCardListeners();
        }
    }
}

function renderEmptyState() {
    const container = document.getElementById('highlightsContainer');
    if (container) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-star"></i>
                <h3>No highlights yet</h3>
                <p>Create your first highlight to mark important moments!</p>
            </div>
        `;
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}

// Chart initialization
function initializeCharts() {
    // Viewer Chart
    const viewerCtx = document.getElementById('viewerChart').getContext('2d');
    viewerChart = new Chart(viewerCtx, {
        type: 'line',
        data: {
            labels: Array.from({length: 30}, (_, i) => `${i}min ago`),
            datasets: [{
                label: 'Viewers',
                data: Array.from({length: 30}, () => Math.floor(Math.random() * 500) + 1000),
                borderColor: '#8B5CF6',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#9CA3AF'
                    }
                },
                x: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#9CA3AF'
                    }
                }
            }
        }
    });

    // Performance Chart
    const performanceCtx = document.getElementById('performanceChart').getContext('2d');
    performanceChart = new Chart(performanceCtx, {
        type: 'line',
        data: {
            labels: ['1h', '2h', '3h', '4h', '5h', '6h'],
            datasets: [{
                label: 'Viewers',
                data: [1200, 1500, 1300, 1800, 1600, 1400],
                borderColor: '#8B5CF6',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#9CA3AF'
                    }
                },
                x: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#9CA3AF'
                    }
                }
            }
        }
    });

    // Interactions Chart
    const interactionsCtx = document.getElementById('interactionsChart').getContext('2d');
    interactionsChart = new Chart(interactionsCtx, {
        type: 'bar',
        data: {
            labels: ['Likes', 'Comments', 'Shares', 'Follows'],
            datasets: [{
                data: [150, 80, 40, 60],
                backgroundColor: ['#8B5CF6', '#EC4899', '#3B82F6', '#10B981']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#9CA3AF'
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        color: '#9CA3AF'
                    }
                }
            }
        }
    });

    // Retention Chart
    const retentionCtx = document.getElementById('retentionChart').getContext('2d');
    retentionChart = new Chart(retentionCtx, {
        type: 'line',
        data: {
            labels: ['0%', '20%', '40%', '60%', '80%', '100%'],
            datasets: [{
                label: 'Viewers',
                data: [1000, 800, 600, 400, 200, 100],
                borderColor: '#8B5CF6',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#9CA3AF'
                    }
                },
                x: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#9CA3AF'
                    }
                }
            }
        }
    });

    // Age Demographics Chart
    const ageCtx = document.getElementById('ageChart').getContext('2d');
    ageChart = new Chart(ageCtx, {
        type: 'doughnut',
        data: {
            labels: ['18-24', '25-34', '35-44', '45-54', '55+'],
            datasets: [{
                data: [30, 25, 20, 15, 10],
                backgroundColor: ['#8B5CF6', '#EC4899', '#3B82F6', '#10B981', '#F59E0B']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        color: '#9CA3AF'
                    }
                }
            }
        }
    });

    // Device Type Chart
    const deviceCtx = document.getElementById('deviceChart').getContext('2d');
    deviceChart = new Chart(deviceCtx, {
        type: 'doughnut',
        data: {
            labels: ['Desktop', 'Mobile', 'Tablet'],
            datasets: [{
                data: [50, 40, 10],
                backgroundColor: ['#8B5CF6', '#EC4899', '#3B82F6']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        color: '#9CA3AF'
                    }
                }
            }
        }
    });

    // Viewer Loyalty Chart
    const loyaltyCtx = document.getElementById('loyaltyChart').getContext('2d');
    loyaltyChart = new Chart(loyaltyCtx, {
        type: 'line',
        data: {
            labels: ['1st', '2nd', '3rd', '4th', '5th+'],
            datasets: [{
                label: 'Viewers',
                data: [1000, 800, 600, 400, 200],
                borderColor: '#8B5CF6',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#9CA3AF'
                    }
                },
                x: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#9CA3AF'
                    }
                }
            }
        }
    });
}

// WebSocket connection function
function connectWebSocket() {
    try {
        console.log('Attempting to connect to WebSocket...');
        const socket = new SockJS('/live/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, 
            function(frame) {
                console.log('Connected to WebSocket successfully');
                reconnectAttempts = 0;
                
                // Subscribe to metrics updates
                stompClient.subscribe('/topic/metrics', function(message) {
                    console.log('Received metrics update:', message.body);
                    const metrics = JSON.parse(message.body);
                    updateMetricsUI(metrics);
                });

                // Subscribe to viewer count updates
                stompClient.subscribe('/topic/viewers', function(message) {
                    console.log('Received viewer update:', message.body);
                    const viewerData = JSON.parse(message.body);
                    updateViewerChart(viewerData);
                });

                // Subscribe to engagement updates
                stompClient.subscribe('/topic/engagement', function(message) {
                    console.log('Received engagement update:', message.body);
                    const engagement = JSON.parse(message.body);
                    updateEngagementChart(engagement);
                });

                // Subscribe to performance updates
                stompClient.subscribe('/topic/performance', function(message) {
                    console.log('Received performance update:', message.body);
                    const performance = JSON.parse(message.body);
                    updatePerformanceChart(performance);
                });

                // Subscribe to chat messages
                stompClient.subscribe('/topic/messages', function(message) {
                    console.log('Received chat message:', message.body);
                    const chatMessage = JSON.parse(message.body);
                    addChatMessage(chatMessage);
                });

                // Subscribe to top chatters
                stompClient.subscribe('/topic/top-chatters', function(message) {
                    console.log('Received top chatters update:', message.body);
                    const topChatters = JSON.parse(message.body);
                    updateTopChatters(topChatters);
                });

                // Subscribe to highlights updates
                stompClient.subscribe('/topic/highlights', function(message) {
                    console.log('Received highlights update:', message.body);
                    const highlightUpdate = JSON.parse(message.body);
                    handleHighlightUpdate(highlightUpdate);
                });
            },
            function(error) {
                console.error('WebSocket connection error:', error);
                if (reconnectAttempts < maxReconnectAttempts) {
                    reconnectAttempts++;
                    console.log(`Attempting to reconnect (${reconnectAttempts}/${maxReconnectAttempts})...`);
                    setTimeout(connectWebSocket, 5000);
                } else {
                    console.error('Max reconnection attempts reached');
                }
            }
        );
    } catch (error) {
        console.error('Error creating WebSocket connection:', error);
        if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++;
            setTimeout(connectWebSocket, 5000);
        }
    }
}

// Like/Dislike functions
function incrementLike() {
    console.log('Sending like request...');
    fetch('/live/like', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        console.log('Like response:', data);
        updateMetricsUI(data);
    })
    .catch(error => {
        console.error('Error sending like:', error);
    });
}

function incrementDislike() {
    console.log('Sending dislike request...');
    fetch('/live/dislike', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        console.log('Dislike response:', data);
        updateMetricsUI(data);
    })
    .catch(error => {
        console.error('Error sending dislike:', error);
    });
}

// Chat functionality
function initializeChat() {
    const chatInput = document.getElementById('chatInput');
    const sendChatButton = document.getElementById('sendChatButton');
    const chatMessages = document.getElementById('chatMessages');

    // Send message function
    function sendMessage() {
        const message = chatInput.value.trim();
        if (message && stompClient && stompClient.connected) {
            const chatMessage = {
                username: 'User', 
                content: message,
                timestamp: new Date().toLocaleTimeString()
            };

            try {
                console.log('Sending chat message:', chatMessage);
                stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
                chatInput.value = '';
            } catch (error) {
                console.error('Error sending message:', error);
            }
        } else {
            console.warn('WebSocket not connected. Message not sent.');
        }
    }

    // Add message to chat
    function addChatMessage(message) {
        const messageDiv = document.createElement('div');
        messageDiv.className = 'chat-message';
        messageDiv.innerHTML = `
            <div class="flex items-center justify-between mb-2">
                <p class="text-sm text-[#D4A373]">${message.timestamp}</p>
                <i class="fas fa-ellipsis-v text-[#CCD5AE]"></i>
            </div>
            <p class="text-[#2C3E50]">
                <span class="font-bold">${message.username}:</span>
                <span>${message.content}</span>
            </p>
        `;
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // Event listeners
    if (sendChatButton) {
        sendChatButton.addEventListener('click', sendMessage);
    }
    if (chatInput) {
        chatInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
    }
}

// Update top chatters
function updateTopChatters(topChatters) {
    const topChattersContainer = document.querySelector('.stat-card .space-y-3');
    if (topChattersContainer) {
        topChattersContainer.innerHTML = '';
        topChatters.forEach(chatter => {
            const chatterDiv = document.createElement('div');
            chatterDiv.className = 'stat-card p-4 flex justify-between items-center';
            chatterDiv.innerHTML = `
                <div class="flex items-center space-x-3">
                    <div class="w-8 h-8 rounded-full bg-[#D4A373] flex items-center justify-center text-white">
                        <i class="fas fa-user"></i>
                    </div>
                    <span>${chatter.username}</span>
                </div>
                <span class="text-sm">${chatter.messageCount} messages</span>
            `;
            topChattersContainer.appendChild(chatterDiv);
        });
    }
}

// Recording functions
function startRecording() {
    try {
        // Get the video element from video.js player
        const videoElement = videoPlayer.tech().el();
        if (!videoElement) {
            throw new Error('Video element not found');
        }

        const stream = videoElement.captureStream(30); // 30 FPS
        
        if (!MediaRecorder.isTypeSupported('video/webm;codecs=vp9')) {
            throw new Error('VP9 codec not supported. Falling back to VP8.');
        }

        const mimeType = MediaRecorder.isTypeSupported('video/webm;codecs=vp9') 
            ? 'video/webm;codecs=vp9'
            : 'video/webm;codecs=vp8';

        mediaRecorder = new MediaRecorder(stream, {
            mimeType: mimeType,
            videoBitsPerSecond: 2500000 // 2.5 Mbps
        });

        mediaRecorder.ondataavailable = (event) => {
            if (event.data.size > 0) {
                recordedChunks.push(event.data);
            }
        };

        mediaRecorder.onstop = () => {
            const blob = new Blob(recordedChunks, { type: 'video/webm' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            document.body.appendChild(a);
            a.style.display = 'none';
            a.href = url;
            a.download = `stream-recording-${new Date().toISOString().slice(0,19).replace(/[:]/g, '-')}.webm`;
            a.click();
            window.URL.revokeObjectURL(url);
            recordedChunks = [];
        };

        mediaRecorder.start(1000);
        isRecording = true;
        document.getElementById('recordIcon').style.color = '#ff0000';
        document.getElementById('recordText').textContent = 'Stop Recording';
        document.getElementById('recordButton').classList.remove('bg-red-600', 'hover:bg-red-700');
        document.getElementById('recordButton').classList.add('bg-gray-600', 'hover:bg-gray-700');
    } catch (error) {
        console.error('Error starting recording:', error);
        alert('Failed to start recording. Please check browser compatibility and permissions.');
    }
}

function stopRecording() {
    try {
        if (mediaRecorder && mediaRecorder.state !== 'inactive') {
            mediaRecorder.stop();
            isRecording = false;
            document.getElementById('recordIcon').style.color = '#ffffff';
            document.getElementById('recordText').textContent = 'Record';
            document.getElementById('recordButton').classList.remove('bg-gray-600', 'hover:bg-gray-700');
            document.getElementById('recordButton').classList.add('bg-red-600', 'hover:bg-red-700');
        }
    } catch (error) {
        console.error('Error stopping recording:', error);
        alert('Failed to stop recording. Please try again.');
    }
}

function fetchRecordings() {
    const recordingsList = document.getElementById('recordingsList');
    if (!recordingsList) return;
    fetch('/live/recordings')
        .then(response => response.json())
        .then(files => {
            if (!files || files.length === 0) {
                recordingsList.innerHTML = '<p>No recordings found.</p>';
                return;
            }
            recordingsList.innerHTML = '';
            files.forEach(filename => {
                const item = document.createElement('div');
                item.className = 'flex items-center justify-between mb-2 p-2 bg-gray-100 rounded';
                item.innerHTML = `
                    <span class="truncate">${filename}</span>
                    <div class="flex space-x-2">
                        <button class="play-btn btn-primary px-3 py-1 rounded" data-filename="${filename}"><i class="fas fa-play"></i> Play</button>
                        <a class="download-btn btn-primary px-3 py-1 rounded" href="/live/recordings/${filename}" download><i class="fas fa-download"></i> Download</a>
                    </div>
                `;
                recordingsList.appendChild(item);
            });
            // Add play event listeners
            document.querySelectorAll('.play-btn').forEach(btn => {
                btn.addEventListener('click', function() {
                    playRecording(this.getAttribute('data-filename'));
                });
            });
        })
        .catch(err => {
            recordingsList.innerHTML = '<p>Error loading recordings.</p>';
        });
}

function playRecording(filename) {
    // Create a modal or inline player for playback
    let modal = document.getElementById('recordingModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'recordingModal';
        modal.style.position = 'fixed';
        modal.style.top = '0';
        modal.style.left = '0';
        modal.style.width = '100vw';
        modal.style.height = '100vh';
        modal.style.background = 'rgba(0,0,0,0.7)';
        modal.style.display = 'flex';
        modal.style.alignItems = 'center';
        modal.style.justifyContent = 'center';
        modal.style.zIndex = '9999';
        modal.innerHTML = `
            <div style="background:#fff; padding:20px; border-radius:10px; max-width:90vw; max-height:90vh; position:relative;">
                <button id="closeRecordingModal" style="position:absolute; top:10px; right:10px; font-size:1.5rem;">&times;</button>
                <video id="recordingPlayer" class="video-js vjs-default-skin" controls style="width:70vw; max-width:800px; max-height:70vh;"></video>
            </div>
        `;
        document.body.appendChild(modal);
        document.getElementById('closeRecordingModal').onclick = function() {
            modal.remove();
        };
    } else {
        modal.style.display = 'flex';
    }
    // Set video source
    const player = document.getElementById('recordingPlayer');
    if (player) {
        player.innerHTML = '';
        const source = document.createElement('source');
        source.src = `/live/recordings/${filename}`;
        source.type = 'video/mp4';
        player.appendChild(source);
        player.load();
        // Re-initialize video.js if needed
        if (window.recordingVjsPlayer) {
            window.recordingVjsPlayer.dispose();
        }
        window.recordingVjsPlayer = videojs(player);
        window.recordingVjsPlayer.play();
    }
}

function updateMetricsUI(metrics) {
    if (document.getElementById('likeCount'))
        document.getElementById('likeCount').textContent = metrics.likes;
    if (document.getElementById('dislikeCount'))
        document.getElementById('dislikeCount').textContent = metrics.dislikes;
    if (document.getElementById('likeRatio'))
        document.getElementById('likeRatio').textContent = metrics.likeRatio + '%';
    if (document.getElementById('likeDisplay'))
        document.getElementById('likeDisplay').textContent = metrics.likes;
    if (document.getElementById('dislikeDisplay'))
        document.getElementById('dislikeDisplay').textContent = metrics.dislikes;
    if (document.getElementById('likeRatioDisplay'))
        document.getElementById('likeRatioDisplay').textContent = metrics.likeRatio + '%';
}

function updateViewerChart(viewerData) {
    if (viewerChart) {
        viewerChart.data.labels = viewerData.labels;
        viewerChart.data.datasets[0].data = viewerData.data;
        viewerChart.update();
    }
}

function updateEngagementChart(engagement) {
    if (interactionsChart) {
        interactionsChart.data.datasets[0].data = engagement.data;
        interactionsChart.update();
    }
}

function updatePerformanceChart(performance) {
    if (performanceChart) {
        performanceChart.data.labels = performance.labels;
        performanceChart.data.datasets[0].data = performance.data;
        performanceChart.update();
    }
}

function handleHighlightUpdate(highlightUpdate) {
    console.log('Received highlight update:', highlightUpdate);
    
    if (highlightUpdate.type === 'CREATE') {
        highlights.unshift(highlightUpdate.highlight);
        renderHighlights();
        showNotification('New highlight created!', 'success');
    } else if (highlightUpdate.type === 'UPDATE') {
        const index = highlights.findIndex(h => h.id === highlightUpdate.highlight.id);
        if (index !== -1) {
            highlights[index] = highlightUpdate.highlight;
            renderHighlights();
        }
    } else if (highlightUpdate.type === 'DELETE') {
        highlights = highlights.filter(h => h.id !== highlightUpdate.highlightId);
        renderHighlights();
        showNotification('Highlight deleted', 'info');
    }
}

// Quality Management Functions
function initializeQualityManagement() {
    console.log('Initializing quality management...');
    
    // Quality radio buttons
    const qualityRadios = document.querySelectorAll('.quality-radio');
    qualityRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            const quality = this.value;
            changeQuality(quality);
        });
    });

    // Auto-switch checkbox
    const autoSwitchCheckbox = document.getElementById('autoSwitch');
    if (autoSwitchCheckbox) {
        autoSwitchCheckbox.addEventListener('change', function() {
            userQualityPreference.autoSwitch = this.checked;
            updateQualityPreference();
        });
    }

    // Bandwidth threshold input
    const bandwidthThresholdInput = document.getElementById('bandwidthThreshold');
    if (bandwidthThresholdInput) {
        bandwidthThresholdInput.addEventListener('input', function() {
            userQualityPreference.bandwidthThreshold = parseInt(this.value);
            updateQualityPreference();
        });
    }

    // Quality recording buttons
    const qualityRecordBtn = document.getElementById('qualityRecordBtn');
    const multiQualityRecordBtn = document.getElementById('multiQualityRecordBtn');
    
    if (qualityRecordBtn) {
        qualityRecordBtn.addEventListener('click', function() {
            if (!isQualityRecording) {
                startQualityRecording();
            } else {
                stopQualityRecording();
            }
        });
    }
    
    if (multiQualityRecordBtn) {
        multiQualityRecordBtn.addEventListener('click', startMultiQualityRecording);
    }

    // Load user quality preference
    loadUserQualityPreference();
    
    // Start bandwidth monitoring
    startBandwidthMonitoring();
    
    // Load quality switch history
    loadQualitySwitchHistory();
    
    // Subscribe to quality updates
    if (stompClient && stompClient.connected) {
        stompClient.subscribe('/topic/quality-update', function(message) {
            const qualityUpdate = JSON.parse(message.body);
            if (qualityUpdate.userId === userId) {
                updateQualityUI(qualityUpdate.quality);
            }
        });
        
        stompClient.subscribe('/topic/quality-switch', function(message) {
            const switchLog = JSON.parse(message.body);
            if (switchLog.userId === userId) {
                logQualitySwitch(switchLog.fromQuality, switchLog.toQuality);
            }
        });
    }
}

function changeQuality(quality) {
    console.log('Changing quality to:', quality);
    
    const previousQuality = currentQuality;
    currentQuality = quality;
    
    // Update UI
    updateQualityUI(quality);
    
    // Send quality change to backend
    fetch('/api/quality/set', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `userId=${userId}&quality=${quality}`
    })
    .then(response => response.json())
    .then(data => {
        console.log('Quality change response:', data);
        if (data.status === 'success') {
            // Log quality switch
            if (previousQuality !== quality) {
                logQualitySwitch(previousQuality, quality);
            }
        }
    })
    .catch(error => {
        console.error('Error changing quality:', error);
    });
}

function updateQualityUI(quality) {
    const currentQualitySpan = document.getElementById('currentQuality');
    if (currentQualitySpan) {
        currentQualitySpan.textContent = quality.charAt(0).toUpperCase() + quality.slice(1);
        
        // Update color based on quality
        currentQualitySpan.className = 'px-2 py-1 rounded text-sm text-white';
        switch (quality) {
            case 'high':
                currentQualitySpan.classList.add('bg-green-600');
                break;
            case 'medium':
                currentQualitySpan.classList.add('bg-yellow-600');
                break;
            case 'low':
                currentQualitySpan.classList.add('bg-red-600');
                break;
            default:
                currentQualitySpan.classList.add('bg-[#D4A373]');
        }
    }
}

function startBandwidthMonitoring() {
    // Simulate bandwidth monitoring (in real implementation, use WebRTC stats)
    setInterval(() => {
        updateBandwidthDisplay();
    }, 3000);
}

function updateBandwidthDisplay() {
    // Simulate bandwidth measurement (in real implementation, this would be actual network measurement)
    const baseBandwidth = 3000;
    const variation = Math.random() * 2000 - 1000;
    currentBandwidth = Math.max(500, baseBandwidth + variation);
    
    const bandwidthDisplay = document.getElementById('currentBandwidth');
    if (bandwidthDisplay) {
        bandwidthDisplay.textContent = currentBandwidth.toFixed(0) + ' kbps';
    }
    
    // Send bandwidth data to backend
    fetch('/api/quality/bandwidth', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `userId=${userId}&bandwidthKbps=${Math.round(currentBandwidth)}`
    })
    .then(response => response.json())
    .then(data => {
        console.log('Bandwidth update response:', data);
    })
    .catch(error => {
        console.error('Error updating bandwidth:', error);
    });
    
    // Auto-switch quality if enabled
    if (userQualityPreference.autoSwitch) {
        const optimalQuality = determineOptimalQuality(currentBandwidth);
        if (optimalQuality !== currentQuality) {
            changeQuality(optimalQuality);
        }
    }
}

function determineOptimalQuality(bandwidth) {
    if (bandwidth >= 4500) return 'high';
    if (bandwidth >= 2000) return 'medium';
    return 'low';
}

function logQualitySwitch(fromQuality, toQuality) {
    console.log(`Quality switch: ${fromQuality} -> ${toQuality}`);
    
    const switchEntry = {
        fromQuality: fromQuality,
        toQuality: toQuality,
        timestamp: new Date().toLocaleTimeString(),
        bandwidth: currentBandwidth
    };
    
    qualitySwitchHistory.unshift(switchEntry);
    if (qualitySwitchHistory.length > 10) {
        qualitySwitchHistory.pop();
    }
    
    renderQualitySwitchHistory();
    
    // Send quality switch to backend
    fetch('/api/quality/switch', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            streamId: currentStreamId,
            fromQuality: fromQuality,
            toQuality: toQuality,
            switchReason: 'MANUAL',
            bandwidthAvailable: Math.round(currentBandwidth),
            latencyMs: Math.floor(Math.random() * 100) + 50,
            packetLoss: 0.0
        })
    })
    .then(response => response.json())
    .then(data => {
        console.log('Quality switch logged:', data);
    })
    .catch(error => {
        console.error('Error logging quality switch:', error);
    });
}

function updateQualityPreference() {
    console.log('Updating quality preference:', userQualityPreference);
    
    fetch('/api/quality/preference', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            streamId: currentStreamId,
            preferredQuality: userQualityPreference.preferredQuality,
            autoSwitch: userQualityPreference.autoSwitch,
            bandwidthThreshold: userQualityPreference.bandwidthThreshold
        })
    })
    .then(response => response.json())
    .then(data => {
        console.log('Quality preference update response:', data);
    })
    .catch(error => {
        console.error('Error updating quality preference:', error);
    });
}

function loadUserQualityPreference() {
    console.log('Loading user quality preference...');
    
    fetch(`/api/quality/preference/${currentStreamId}`)
    .then(response => response.json())
    .then(data => {
        if (data.success && data.preference) {
            const preference = data.preference;
            userQualityPreference = {
                preferredQuality: preference.preferredQuality || 'auto',
                autoSwitch: preference.autoSwitch !== false,
                bandwidthThreshold: preference.bandwidthThreshold || 5000
            };
            
            currentQuality = userQualityPreference.preferredQuality;
            
            // Update UI
            const autoSwitchCheckbox = document.getElementById('autoSwitch');
            const bandwidthThresholdInput = document.getElementById('bandwidthThreshold');
            
            if (autoSwitchCheckbox) {
                autoSwitchCheckbox.checked = userQualityPreference.autoSwitch;
            }
            
            if (bandwidthThresholdInput) {
                bandwidthThresholdInput.value = userQualityPreference.bandwidthThreshold;
            }
            
            // Update quality radio buttons
            const qualityRadio = document.querySelector(`input[name="quality"][value="${currentQuality}"]`);
            if (qualityRadio) {
                qualityRadio.checked = true;
            }
            
            updateQualityUI(currentQuality);
        }
    })
    .catch(error => {
        console.error('Error loading quality preference:', error);
    });
}

function startQualityRecording() {
    console.log('Starting quality recording...');
    
    fetch('/api/quality/recording/start', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            streamId: currentStreamId,
            qualityLevel: currentQuality,
            createdBy: userId
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            isQualityRecording = true;
            currentRecordingId = data.recording.id;
            qualityRecordingStartTime = Date.now();
            
            updateQualityRecordingUI();
            updateRecordingTimer();
            
            console.log('Quality recording started:', data.recording);
        } else {
            console.error('Error starting quality recording:', data.message);
        }
    })
    .catch(error => {
        console.error('Error starting quality recording:', error);
    });
}

function stopQualityRecording() {
    if (!isQualityRecording || !currentRecordingId) {
        console.log('No active quality recording to stop');
        return;
    }
    
    console.log('Stopping quality recording...');
    
    fetch(`/api/quality/recording/${currentRecordingId}/stop`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            duration: Date.now() - qualityRecordingStartTime,
            finalQuality: currentQuality
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            isQualityRecording = false;
            currentRecordingId = null;
            qualityRecordingStartTime = null;
            
            updateQualityRecordingUI();
            clearInterval(recordingTimerInterval);
            
            console.log('Quality recording stopped:', data.recording);
        } else {
            console.error('Error stopping quality recording:', data.message);
        }
    })
    .catch(error => {
        console.error('Error stopping quality recording:', error);
    });
}

function startMultiQualityRecording() {
    console.log('Starting multi-quality recording...');
    
    const qualities = ['high', 'medium', 'low'];
    qualities.forEach(quality => {
        const recordingData = {
            streamId: currentStreamId,
            qualityLevel: quality,
            createdBy: 'user'
        };
        
        fetch('/api/quality/recording/start', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(recordingData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                console.log(`Started recording in ${quality} quality`);
            }
        })
        .catch(error => {
            console.error(`Error starting ${quality} recording:`, error);
        });
    });
    
    showNotification('Multi-quality recording started', 'success');
}

function updateQualityRecordingUI() {
    const qualityRecordBtn = document.getElementById('qualityRecordBtn');
    const recordingStatus = document.getElementById('recordingStatus');
    
    if (isQualityRecording) {
        if (qualityRecordBtn) {
            qualityRecordBtn.innerHTML = '<i class="fas fa-stop mr-1"></i>Stop Recording';
            qualityRecordBtn.classList.remove('btn-primary');
            qualityRecordBtn.classList.add('btn-secondary');
        }
        
        if (recordingStatus) {
            recordingStatus.classList.remove('hidden');
        }
        
        // Start recording timer
        qualityRecordingInterval = setInterval(updateRecordingTimer, 1000);
    } else {
        if (qualityRecordBtn) {
            qualityRecordBtn.innerHTML = '<i class="fas fa-record-vinyl mr-1"></i>Start Recording';
            qualityRecordBtn.classList.remove('btn-secondary');
            qualityRecordBtn.classList.add('btn-primary');
        }
        
        if (recordingStatus) {
            recordingStatus.classList.add('hidden');
        }
        
        if (qualityRecordingInterval) {
            clearInterval(qualityRecordingInterval);
            qualityRecordingInterval = null;
        }
    }
}

function updateRecordingTimer() {
    if (!isQualityRecording || !qualityRecordingStartTime) return;
    
    const elapsed = Math.floor((Date.now() - qualityRecordingStartTime) / 1000);
    const minutes = Math.floor(elapsed / 60);
    const seconds = elapsed % 60;
    
    const recordingTime = document.getElementById('recordingTime');
    if (recordingTime) {
        recordingTime.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }
}

function loadQualitySwitchHistory() {
    console.log('Loading quality switch history...');
    
    fetch(`/api/quality/switches/${currentStreamId}`)
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            qualitySwitchHistory = data.switches || [];
            renderQualitySwitchHistory();
        } else {
            console.error('Error loading quality switch history:', data.message);
        }
    })
    .catch(error => {
        console.error('Error loading quality switch history:', error);
    });
}

function renderQualitySwitchHistory() {
    const container = document.getElementById('qualitySwitchHistory');
    if (!container) return;
    
    if (qualitySwitchHistory.length === 0) {
        container.innerHTML = '<p class="text-gray-500 text-center py-4">No quality switches yet</p>';
        return;
    }
    
    const historyHTML = qualitySwitchHistory.map(switchLog => `
        <div class="flex items-center justify-between text-sm p-2 bg-gray-50 rounded">
            <div class="flex items-center space-x-2">
                <span class="font-medium">${switchLog.fromQuality} → ${switchLog.toQuality}</span>
                <span class="text-gray-500">${switchLog.switchReason}</span>
            </div>
            <div class="text-gray-500">
                ${formatDate(switchLog.switchedAt)}
            </div>
        </div>
    `).join('');
    
    container.innerHTML = historyHTML;
}

function updateQualityAnalytics() {
    fetch(`/api/quality/analytics/${currentStreamId}`)
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            const analytics = data.analytics.find(a => a.qualityLevel === currentQuality);
            if (analytics) {
                updateQualityAnalyticsUI(analytics);
            }
        }
    })
    .catch(error => {
        console.error('Error updating quality analytics:', error);
    });
}

function updateQualityAnalyticsUI(analytics) {
    const avgLatency = document.getElementById('avgLatency');
    const packetLoss = document.getElementById('packetLoss');
    const switchCount = document.getElementById('switchCount');
    const errorCount = document.getElementById('errorCount');
    
    if (avgLatency) avgLatency.textContent = analytics.avgLatency + ' ms';
    if (packetLoss) packetLoss.textContent = analytics.avgPacketLoss + '%';
    if (switchCount) switchCount.textContent = analytics.switchCount;
    if (errorCount) errorCount.textContent = analytics.errorCount;
}

// Initialize character count for chat
function initializeChatCharacterCount() {
    const chatInput = document.getElementById('chatInput');
    const chatCharacterCount = document.getElementById('chatCharacterCount');

    if (chatInput && chatCharacterCount) {
        chatInput.addEventListener('input', function() {
            const currentLength = this.value.length;
            const maxLength = 200;
            chatCharacterCount.textContent = `${currentLength}/${maxLength}`;
            
            // Add warning classes
            chatCharacterCount.className = 'text-xs text-gray-500 text-center';
            if (currentLength > maxLength * 0.8) {
                chatCharacterCount.classList.add('warning');
                chatCharacterCount.classList.remove('text-gray-500');
                chatCharacterCount.classList.add('text-yellow-600');
            }
            if (currentLength > maxLength * 0.95) {
                chatCharacterCount.classList.remove('warning');
                chatCharacterCount.classList.add('danger');
                chatCharacterCount.classList.remove('text-yellow-600');
                chatCharacterCount.classList.add('text-red-600');
            }
        });
    }
}

// Initialize clear chat and refresh buttons
function initializeChatControls() {
    const clearChatButton = document.getElementById('clearChat');
    const refreshTopChattersButton = document.getElementById('refreshTopChatters');

    if (clearChatButton) {
        clearChatButton.addEventListener('click', clearChat);
    }

    if (refreshTopChattersButton) {
        refreshTopChattersButton.addEventListener('click', refreshTopChatters);
    }
}

function clearChat() {
    const chatMessages = document.getElementById('chatMessages');
    if (chatMessages) {
        chatMessages.innerHTML = `
            <div id="noChatMessages" class="text-center py-8 text-gray-500">
                <i class="fas fa-comments text-2xl mb-2"></i>
                <p class="text-sm">No messages yet</p>
                <p class="text-xs">Be the first to chat!</p>
            </div>
        `;
        showNotification('Chat cleared', 'info');
    }
}

function refreshTopChatters() {
    // Add loading state
    const topChattersList = document.getElementById('topChattersList');
    if (topChattersList) {
        topChattersList.classList.add('chat-loading');
        
        // Simulate refresh (in real app, this would fetch from server)
        setTimeout(() => {
            topChattersList.classList.remove('chat-loading');
            showNotification('Top chatters refreshed', 'success');
        }, 1000);
    }
}

// Notification system
function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `fixed top-4 right-4 z-50 p-4 rounded-lg shadow-lg transition-all duration-300 transform translate-x-full`;
    
    // Set colors based on type
    switch(type) {
        case 'success':
            notification.classList.add('bg-green-500', 'text-white');
            break;
        case 'error':
            notification.classList.add('bg-red-500', 'text-white');
            break;
        case 'warning':
            notification.classList.add('bg-yellow-500', 'text-white');
            break;
        default:
            notification.classList.add('bg-blue-500', 'text-white');
    }
    
    notification.innerHTML = `
        <div class="flex items-center space-x-2">
            <i class="fas ${getNotificationIcon(type)}"></i>
            <span>${message}</span>
        </div>
    `;
    
    // Add to page
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.classList.remove('translate-x-full');
    }, 100);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.classList.add('translate-x-full');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

function getNotificationIcon(type) {
    switch(type) {
        case 'success':
            return 'fa-check-circle';
        case 'error':
            return 'fa-exclamation-circle';
        case 'warning':
            return 'fa-exclamation-triangle';
        default:
            return 'fa-info-circle';
    }
}

function loadQualityRecordings() {
    console.log('Loading quality recordings...');
    
    fetch(`/api/quality/recordings/${currentStreamId}`)
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            qualityRecordings = data.recordings || [];
            renderQualityRecordings();
        } else {
            console.error('Error loading quality recordings:', data.message);
        }
    })
    .catch(error => {
        console.error('Error loading quality recordings:', error);
    });
}