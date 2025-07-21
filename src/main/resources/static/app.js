const { useState, useEffect, useCallback } = React;

// API Configuration
const API_BASE_URL = 'http://localhost:8080';

// API Service
const apiService = {
    async request(endpoint, options = {}) {
        const token = localStorage.getItem('token');
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...(token && { Authorization: `Bearer ${token}` }),
            },
            ...options,
        };

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
            
            // 調試信息
            console.log(`API Request: ${endpoint}`, {
                status: response.status,
                ok: response.ok,
                contentLength: response.headers.get('content-length'),
                contentType: response.headers.get('content-type')
            });
            
            // 先檢查響應是否成功
            if (!response.ok) {
                // 嘗試解析錯誤訊息
                let errorMessage = `Request failed with status ${response.status}`;
                try {
                    const contentType = response.headers.get('content-type');
                    if (contentType && contentType.includes('application/json')) {
                        const errorData = await response.json();
                        if (errorData.message === '欄位驗證錯誤' && errorData.errors) {
                            const error = new Error(errorData.message);
                            error.fieldErrors = errorData.errors;
                            throw error;
                        }
                        errorMessage = errorData.message || errorMessage;
                    }
                } catch (e) {
                    // 保持原始錯誤訊息
                }
                throw new Error(errorMessage);
            }
            
            // 檢查是否為 204 No Content
            if (response.status === 204) {
                console.log('Response is 204 No Content');
                return null;
            }
            
            // 檢查是否有內容
            const contentLength = response.headers.get('content-length');
            const contentType = response.headers.get('content-type');
            
            if (contentLength === '0' || !contentType || !contentType.includes('application/json')) {
                console.log('Response has no JSON content');
                return null;
            }
            
            // 嘗試解析 JSON 響應
            try {
                const data = await response.json();
                return data;
            } catch (e) {
                // 如果成功響應但無法解析 JSON，返回 null
                console.warn('Response body is not valid JSON:', e);
                return null;
            }
        } catch (error) {
            console.error('API Error details:', error);
            throw error;
        }
    },

    // Auth endpoints
    login: (credentials) => apiService.request('/auth/login', {
        method: 'POST',
        body: JSON.stringify(credentials),
    }),

    register: (userData) => apiService.request('/auth/register', {
        method: 'POST',
        body: JSON.stringify(userData),
    }),

    getCurrentUser: () => apiService.request('/auth/me'),

    // Notes endpoints
    getNotes: (page = 0, size = 10, search = '') => {
        const searchParam = search ? `&search=${encodeURIComponent(search)}` : '';
        return apiService.request(`/notes?page=${page}&size=${size}${searchParam}`);
    },
    
    getNote: (id) => apiService.request(`/notes/${id}`),
    
    createNote: (note) => apiService.request('/notes', {
        method: 'POST',
        body: JSON.stringify(note),
    }),
    
    updateNote: (id, note) => apiService.request(`/notes/${id}`, {
        method: 'PUT',
        body: JSON.stringify(note),
    }),
    
    deleteNote: (id) => apiService.request(`/notes/${id}`, {
        method: 'DELETE',
    }),
};

// Modern Button Component
const Button = ({ children, variant = 'primary', size = 'md', disabled = false, onClick, ...props }) => {
    const baseClasses = 'font-medium rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2';
    const variants = {
        primary: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500',
        secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300 focus:ring-gray-500',
        danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
        success: 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
        ghost: 'bg-transparent text-gray-700 hover:bg-gray-100 focus:ring-gray-500',
    };
    const sizes = {
        sm: 'px-3 py-1.5 text-sm',
        md: 'px-4 py-2 text-base',
        lg: 'px-6 py-3 text-lg',
    };
    
    return (
        <button
            className={`${baseClasses} ${variants[variant]} ${sizes[size]} ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`}
            disabled={disabled}
            onClick={onClick}
            {...props}
        >
            {children}
        </button>
    );
};

// Modern Input Component
const Input = ({ label, error, icon, ...props }) => {
    return (
        <div className="space-y-1">
            {label && (
                <label className="block text-sm font-medium text-gray-700">
                    {label}
                </label>
            )}
            <div className="relative">
                {icon && (
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <i className={`fas ${icon} text-gray-400`}></i>
                    </div>
                )}
                <input
                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors ${
                        icon ? 'pl-10' : ''
                    } ${error ? 'border-red-500' : 'border-gray-300'}`}
                    {...props}
                />
            </div>
            {error && (
                <p className="text-sm text-red-600">{error}</p>
            )}
        </div>
    );
};

// Modern Card Component
const Card = ({ children, className = '', ...props }) => {
    return (
        <div className={`bg-white rounded-lg shadow-sm border border-gray-200 ${className}`} {...props}>
            {children}
        </div>
    );
};

// Modern Modal Component
const Modal = ({ isOpen, onClose, title, children }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 overflow-y-auto">
            <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
                <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={onClose}></div>
                <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
                    <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                        <div className="flex items-center justify-between mb-4">
                            <h3 className="text-lg font-medium text-gray-900">{title}</h3>
                            <button
                                onClick={onClose}
                                className="text-gray-400 hover:text-gray-600 transition-colors"
                            >
                                <i className="fas fa-times"></i>
                            </button>
                        </div>
                        {children}
                    </div>
                </div>
            </div>
        </div>
    );
};

// Toast Notification Component
const Toast = ({ message, type = 'success', onClose }) => {
    useEffect(() => {
        const timer = setTimeout(onClose, 3000);
        return () => clearTimeout(timer);
    }, [onClose]);

    const typeClasses = {
        success: 'bg-green-500 text-white',
        error: 'bg-red-500 text-white',
        warning: 'bg-yellow-500 text-white',
        info: 'bg-blue-500 text-white',
    };

    return (
        <div className={`fixed top-4 right-4 px-4 py-2 rounded-lg shadow-lg z-50 ${typeClasses[type]}`}>
            <div className="flex items-center space-x-2">
                <span>{message}</span>
                <button onClick={onClose} className="text-white hover:text-gray-200">
                    <i className="fas fa-times"></i>
                </button>
            </div>
        </div>
    );
};

// Auth Form Component
const AuthForm = ({ isLogin, onToggle, onSuccess }) => {
    const [formData, setFormData] = useState({
        email: '',
        password: '',
    });
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: '' }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setErrors({});

        try {
            const response = isLogin 
                ? await apiService.login(formData)
                : await apiService.register(formData);

            if (response.token) {
                localStorage.setItem('token', response.token);
                onSuccess(response, isLogin);
            }
        } catch (error) {
            // 如果有欄位錯誤，設置具體的欄位錯誤
            if (error.fieldErrors) {
                setErrors(error.fieldErrors);
            } else {
                setErrors({ general: error.message });
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <Card className="w-full max-w-md p-8">
            <div className="text-center mb-8">
                <h2 className="text-2xl font-bold text-gray-900 mb-2">
                    {isLogin ? 'Sign In' : 'Sign Up'}
                </h2>
                <p className="text-gray-600">
                    {isLogin ? 'Welcome back!' : 'Create your account'}
                </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
                <Input
                    label="Email"
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    icon="fa-envelope"
                    error={errors.email}
                    required
                />

                <Input
                    label="Password"
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    icon="fa-lock"
                    error={errors.password}
                    required
                />

                {errors.general && (
                    <div className="text-red-600 text-sm text-center">
                        {errors.general}
                    </div>
                )}

                <Button
                    type="submit"
                    disabled={loading}
                    className="w-full"
                >
                    {loading ? 'Processing...' : (isLogin ? 'Sign In' : 'Sign Up')}
                </Button>

                <div className="text-center">
                    <button
                        type="button"
                        onClick={onToggle}
                        className="text-blue-600 hover:text-blue-800 text-sm"
                    >
                        {isLogin ? "Don't have an account? Sign up" : "Already have an account? Sign in"}
                    </button>
                </div>
            </form>
        </Card>
    );
};

// Note Form Component
const NoteForm = ({ note, onSubmit, onCancel }) => {
    const [formData, setFormData] = useState({
        title: note?.title || '',
        content: note?.content || '',
    });
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: '' }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setErrors({});

        try {
            await onSubmit(formData);
        } catch (error) {
            setErrors({ general: error.message });
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <Input
                label="Title"
                name="title"
                value={formData.title}
                onChange={handleChange}
                error={errors.title}
                required
            />

            <div className="space-y-1">
                <label className="block text-sm font-medium text-gray-700">
                    Content
                </label>
                <textarea
                    name="content"
                    value={formData.content}
                    onChange={handleChange}
                    rows={6}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                    required
                />
                {errors.content && (
                    <p className="text-sm text-red-600">{errors.content}</p>
                )}
            </div>

            {errors.general && (
                <div className="text-red-600 text-sm">
                    {errors.general}
                </div>
            )}

            <div className="flex space-x-3">
                <Button type="submit" disabled={loading}>
                    {loading ? 'Saving...' : 'Save Note'}
                </Button>
                <Button type="button" variant="ghost" onClick={onCancel}>
                    Cancel
                </Button>
            </div>
        </form>
    );
};

// Note Card Component
const NoteCard = ({ note, onEdit, onDelete }) => {
    const [showActions, setShowActions] = useState(false);

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
        });
    };

    return (
        <Card className="p-6 hover:shadow-md transition-shadow relative">
            <div className="flex justify-between items-start mb-3">
                <h3 className="text-lg font-semibold text-gray-900 truncate">
                    {note.title}
                </h3>
                <div className="relative">
                    <button
                        onClick={() => setShowActions(!showActions)}
                        className="text-gray-400 hover:text-gray-600 p-1"
                    >
                        <i className="fas fa-ellipsis-h"></i>
                    </button>
                    {showActions && (
                        <div className="absolute right-0 mt-1 w-32 bg-white rounded-lg shadow-lg border border-gray-200 z-10">
                            <button
                                onClick={() => {
                                    onEdit(note);
                                    setShowActions(false);
                                }}
                                className="w-full px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                            >
                                <i className="fas fa-edit"></i>
                                <span>Edit</span>
                            </button>
                            <button
                                onClick={() => {
                                    onDelete(note.id);
                                    setShowActions(false);
                                }}
                                className="w-full px-3 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center space-x-2"
                            >
                                <i className="fas fa-trash"></i>
                                <span>Delete</span>
                            </button>
                        </div>
                    )}
                </div>
            </div>
            
            <p className="text-gray-600 mb-4 line-clamp-3">
                {note.content}
            </p>
            
            <div className="flex items-center text-sm text-gray-500">
                <i className="fas fa-clock mr-1"></i>
                {formatDate(note.createdAt)}
            </div>
        </Card>
    );
};

// Pagination Component
const Pagination = ({ currentPage, totalPages, onPageChange }) => {
    const pageNumbers = [];
    const maxVisiblePages = 5;
    
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
    
    if (endPage - startPage < maxVisiblePages - 1) {
        startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        pageNumbers.push(i);
    }
    
    return (
        <div className="flex items-center justify-center space-x-2 mt-8">
            <Button
                variant="ghost"
                size="sm"
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 0}
            >
                <i className="fas fa-chevron-left"></i>
            </Button>
            
            {startPage > 0 && (
                <>
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onPageChange(0)}
                    >
                        1
                    </Button>
                    {startPage > 1 && <span className="text-gray-500">...</span>}
                </>
            )}
            
            {pageNumbers.map(number => (
                <Button
                    key={number}
                    variant={currentPage === number ? 'primary' : 'ghost'}
                    size="sm"
                    onClick={() => onPageChange(number)}
                >
                    {number + 1}
                </Button>
            ))}
            
            {endPage < totalPages - 1 && (
                <>
                    {endPage < totalPages - 2 && <span className="text-gray-500">...</span>}
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onPageChange(totalPages - 1)}
                    >
                        {totalPages}
                    </Button>
                </>
            )}
            
            <Button
                variant="ghost"
                size="sm"
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
            >
                <i className="fas fa-chevron-right"></i>
            </Button>
        </div>
    );
};

// Main Notes Dashboard Component
const NotesDashboard = ({ user, onLogout }) => {
    const [notes, setNotes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showNoteForm, setShowNoteForm] = useState(false);
    const [editingNote, setEditingNote] = useState(null);
    const [toast, setToast] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [pageSize] = useState(9); // 每頁顯示9筆，適合3列網格

    const showToast = (message, type = 'success') => {
        setToast({ message, type });
    };

    const loadNotes = useCallback(async (page = 0, searchKeyword = '') => {
        try {
            setLoading(true);
            const response = await apiService.getNotes(page, pageSize, searchKeyword);
            
            setNotes(response.content || []);
            setCurrentPage(response.number || 0);
            setTotalPages(response.totalPages || 0);
            setTotalElements(response.totalElements || 0);
        } catch (error) {
            showToast('Failed to load notes', 'error');
        } finally {
            setLoading(false);
        }
    }, [pageSize]);

    useEffect(() => {
        loadNotes(0, searchTerm);
    }, []);

    // 搜尋功能：當搜尋詞變化時重新載入筆記（重置到第一頁）
    useEffect(() => {
        const delayedSearch = setTimeout(() => {
            loadNotes(0, searchTerm);
        }, 300); // 300ms 延遲以避免過度搜尋

        return () => clearTimeout(delayedSearch);
    }, [searchTerm, loadNotes]);

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            loadNotes(newPage, searchTerm);
        }
    };

    const handleCreateNote = async (noteData) => {
        try {
            await apiService.createNote(noteData);
            setShowNoteForm(false);
            // 創建後回到第一頁顯示最新筆記
            loadNotes(0, searchTerm);
            showToast('Note created successfully!');
        } catch (error) {
            throw error;
        }
    };

    const handleUpdateNote = async (noteData) => {
        try {
            await apiService.updateNote(editingNote.id, noteData);
            setEditingNote(null);
            // 更新後重新載入當前頁
            loadNotes(currentPage, searchTerm);
            showToast('Note updated successfully!');
        } catch (error) {
            throw error;
        }
    };

    const handleDeleteNote = async (noteId) => {
        if (window.confirm('Are you sure you want to delete this note?')) {
            try {
                await apiService.deleteNote(noteId);
                // 刪除後檢查是否需要調整頁碼
                if (notes.length === 1 && currentPage > 0) {
                    // 如果當前頁只有一筆且不是第一頁，回到上一頁
                    loadNotes(currentPage - 1, searchTerm);
                } else {
                    loadNotes(currentPage, searchTerm);
                }
                showToast('Note deleted successfully!');
            } catch (error) {
                showToast('Failed to delete note', 'error');
            }
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <header className="bg-white shadow-sm border-b">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center space-x-4">
                            <div className="flex items-center space-x-3">
                                <i className="fas fa-sticky-note text-blue-600 text-xl"></i>
                                <h1 className="text-xl font-bold text-gray-900">Learning Notes</h1>
                            </div>
                        </div>
                        
                        <div className="flex items-center space-x-4">
                            <span className="text-sm text-gray-600">Welcome, {user.email}</span>
                            <Button variant="ghost" onClick={onLogout}>
                                <i className="fas fa-sign-out-alt mr-2"></i>
                                Logout
                            </Button>
                        </div>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Search and Actions */}
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8">
                    <div className="flex-1 max-w-lg">
                        <div className="relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <i className="fas fa-search text-gray-400"></i>
                            </div>
                            <input
                                type="text"
                                placeholder="Search notes..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>
                    </div>
                    
                    <div className="mt-4 sm:mt-0 sm:ml-4">
                        <Button onClick={() => setShowNoteForm(true)}>
                            <i className="fas fa-plus mr-2"></i>
                            New Note
                        </Button>
                    </div>
                </div>

                {/* Notes Grid */}
                {loading ? (
                    <div className="text-center py-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                        <p className="text-gray-600">Loading notes...</p>
                    </div>
                ) : notes.length === 0 ? (
                    <div className="text-center py-12">
                        <i className="fas fa-sticky-note text-gray-300 text-6xl mb-4"></i>
                        <h3 className="text-lg font-medium text-gray-900 mb-2">
                            {searchTerm ? 'No matching notes' : 'No notes yet'}
                        </h3>
                        <p className="text-gray-600 mb-6">
                            {searchTerm ? 'Try adjusting your search' : 'Start by creating your first note'}
                        </p>
                        {!searchTerm && (
                            <Button onClick={() => setShowNoteForm(true)}>
                                <i className="fas fa-plus mr-2"></i>
                                Create Note
                            </Button>
                        )}
                    </div>
                ) : (
                    <>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {notes.map(note => (
                                <NoteCard
                                    key={note.id}
                                    note={note}
                                    onEdit={setEditingNote}
                                    onDelete={handleDeleteNote}
                                />
                            ))}
                        </div>
                        
                        {/* Pagination */}
                        {totalPages > 1 && (
                            <Pagination
                                currentPage={currentPage}
                                totalPages={totalPages}
                                onPageChange={handlePageChange}
                            />
                        )}
                        
                        {/* Page Info */}
                        <div className="text-center mt-4 text-sm text-gray-600">
                            顯示 {notes.length > 0 ? currentPage * pageSize + 1 : 0} - {Math.min((currentPage + 1) * pageSize, totalElements)} 筆，共 {totalElements} 筆
                        </div>
                    </>
                )}
            </main>

            {/* Modals */}
            <Modal
                isOpen={showNoteForm}
                onClose={() => setShowNoteForm(false)}
                title="Create New Note"
            >
                <NoteForm
                    onSubmit={handleCreateNote}
                    onCancel={() => setShowNoteForm(false)}
                />
            </Modal>

            <Modal
                isOpen={!!editingNote}
                onClose={() => setEditingNote(null)}
                title="Edit Note"
            >
                <NoteForm
                    note={editingNote}
                    onSubmit={handleUpdateNote}
                    onCancel={() => setEditingNote(null)}
                />
            </Modal>

            {/* Toast */}
            {toast && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={() => setToast(null)}
                />
            )}
        </div>
    );
};

// Main App Component
const App = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isLogin, setIsLogin] = useState(true);
    const [toast, setToast] = useState(null);

    const showToast = (message, type = 'success') => {
        setToast({ message, type });
    };

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            // 呼叫後端 API 獲取真實用戶資訊
            apiService.getCurrentUser()
                .then(userResponse => {
                    setUser({
                        email: userResponse.email,
                        id: userResponse.id,
                        role: userResponse.role
                    });
                })
                .catch(error => {
                    console.error('Failed to get current user:', error);
                    // Token 可能已過期或無效，清除並要求重新登入
                    localStorage.removeItem('token');
                    setUser(null);
                })
                .finally(() => {
                    setLoading(false);
                });
        } else {
            setLoading(false);
        }
    }, []);

    const handleAuthSuccess = (userResponse, isLogin) => {
        setUser({ 
            email: userResponse.email,
            id: userResponse.id,
            role: userResponse.role 
        });
        
        // 顯示成功訊息
        if (isLogin) {
            showToast('登入成功！', 'success');
        } else {
            showToast('註冊成功！歡迎加入！', 'success');
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        setUser(null);
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            {user ? (
                <NotesDashboard user={user} onLogout={handleLogout} />
            ) : (
                <div className="min-h-screen flex items-center justify-center px-4">
                    <AuthForm
                        isLogin={isLogin}
                        onToggle={() => setIsLogin(!isLogin)}
                        onSuccess={handleAuthSuccess}
                    />
                </div>
            )}
            
            {/* Toast */}
            {toast && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={() => setToast(null)}
                />
            )}
        </div>
    );
};

// Render the App
ReactDOM.render(<App />, document.getElementById('root'));