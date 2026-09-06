import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import RequireAuth from './routes/RequireAuth'
import LoginPage from './pages/LoginPage'
import PoiListPage from './pages/PoiListPage'
import PoiDetailPage from './pages/PoiDetailPage'
import WishlistPage from './pages/WishlistPage'
import ItineraryListPage from './pages/ItineraryListPage'
import ItineraryCreatePage from './pages/ItineraryCreatePage'
import ItineraryDetailPage from './pages/ItineraryDetailPage'
import SharedItineraryPage from './pages/SharedItineraryPage'
import MyPage from './pages/MyPage'
import OAuthCallbackPage from './pages/OAuthCallbackPage'

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<PoiListPage />} />
      <Route path="/pois/:poiId" element={<PoiDetailPage />} />
      <Route
        path="/wishlist"
        element={
          <RequireAuth>
            <WishlistPage />
          </RequireAuth>
        }
      />
      <Route
        path="/itineraries"
        element={
          <RequireAuth>
            <ItineraryListPage />
          </RequireAuth>
        }
      />
      <Route
        path="/itineraries/new"
        element={
          <RequireAuth>
            <ItineraryCreatePage />
          </RequireAuth>
        }
      />
      <Route
        path="/itineraries/:id"
        element={
          <RequireAuth>
            <ItineraryDetailPage />
          </RequireAuth>
        }
      />
      <Route path="/share/:token" element={<SharedItineraryPage />} />
      <Route
        path="/mypage"
        element={
          <RequireAuth>
            <MyPage />
          </RequireAuth>
        }
      />
      <Route path="/oauth/callback/:provider" element={<OAuthCallbackPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
