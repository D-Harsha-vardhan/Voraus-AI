import React, { useState } from 'react';
import { 
  LayoutDashboard, 
  Bot, 
  FileCheck, 
  GraduationCap, 
  Briefcase,
  ChevronRight,
  UploadCloud,
  CheckCircle2,
  Clock,
  Sparkles
} from 'lucide-react';
import './index.css';

function App() {
  const [activeTab, setActiveTab] = useState('dashboard');

  return (
    <div className="app-container">
      {/* Sidebar */}
      <aside className="sidebar">
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '2rem' }}>
            <Sparkles className="text-highlight" style={{ color: 'var(--text-highlight)' }} />
            <span className="text-gradient">Voraus AI</span>
          </h2>
        </div>
        
        <nav>
          <ul className="nav-menu">
            <li className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`} onClick={() => setActiveTab('dashboard')}>
              <LayoutDashboard size={20} />
              Your Journey
            </li>
            <li className={`nav-item ${activeTab === 'advisor' ? 'active' : ''}`} onClick={() => setActiveTab('advisor')}>
              <Bot size={20} />
              AI Advisor
            </li>
            <li className={`nav-item ${activeTab === 'documents' ? 'active' : ''}`} onClick={() => setActiveTab('documents')}>
              <FileCheck size={20} />
              Documents
            </li>
            <li className={`nav-item ${activeTab === 'opportunities' ? 'active' : ''}`} onClick={() => setActiveTab('opportunities')}>
              <GraduationCap size={20} />
              Programs
            </li>
            <li className={`nav-item ${activeTab === 'cv' ? 'active' : ''}`} onClick={() => setActiveTab('cv')}>
              <Briefcase size={20} />
              CV Generator
            </li>
          </ul>
        </nav>

        <div style={{ marginTop: 'auto', padding: '1rem', background: 'rgba(255,255,255,0.03)', borderRadius: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ width: 40, height: 40, borderRadius: '50%', background: 'linear-gradient(135deg, var(--text-highlight), var(--text-secondary))', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#000', fontWeight: 'bold' }}>
              JD
            </div>
            <div>
              <p style={{ fontWeight: 600, fontSize: '0.9rem', color: '#fff' }}>Jane Doe</p>
              <p style={{ fontSize: '0.8rem', opacity: 0.7 }}>Free Plan</p>
            </div>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '3rem' }}>
          <div>
            <h1>Welcome back, Jane</h1>
            <p style={{ opacity: 0.8, marginTop: '0.5rem' }}>Let's continue your journey to Germany.</p>
          </div>
          <button className="btn btn-outline">
            <Bot size={18} /> Ask AI Advisor
          </button>
        </header>

        {/* Dashboard Progress */}
        <section className="glass-panel" style={{ padding: '2rem', marginBottom: '2rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ margin: 0 }}>Journey Progress</h3>
            <span style={{ color: 'var(--text-highlight)', fontWeight: 'bold', fontSize: '1.2rem' }}>40%</span>
          </div>
          <div className="progress-container">
            <div className="progress-bar-bg">
              <div className="progress-bar-fill" style={{ width: '40%' }}></div>
            </div>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1rem', fontSize: '0.85rem', opacity: 0.7 }}>
            <span>Profile Setup</span>
            <span>Document Verification</span>
            <span>APS Application</span>
            <span>University Matching</span>
          </div>
        </section>

        <div className="dashboard-grid">
          {/* Next Steps */}
          <section className="glass-card col-span-2">
            <h3>Immediate Next Steps</h3>
            <p style={{ fontSize: '0.9rem', opacity: 0.8, marginBottom: '1.5rem' }}>Complete these to unblock your APS certification.</p>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1rem', background: 'rgba(255,255,255,0.05)', borderRadius: '8px', borderLeft: '3px solid var(--text-highlight)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <div style={{ background: 'rgba(102, 252, 241, 0.1)', padding: '0.5rem', borderRadius: '8px', color: 'var(--text-highlight)' }}>
                    <UploadCloud size={20} />
                  </div>
                  <div>
                    <h4 style={{ margin: 0, fontSize: '1rem' }}>Upload Bachelor's Degree</h4>
                    <p style={{ margin: 0, fontSize: '0.85rem', opacity: 0.7 }}>Required for APS Evaluation</p>
                  </div>
                </div>
                <button className="btn btn-primary" style={{ padding: '0.5rem 1rem' }}>Upload</button>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1rem', background: 'rgba(255,255,255,0.02)', borderRadius: '8px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <div style={{ background: 'rgba(255, 255, 255, 0.05)', padding: '0.5rem', borderRadius: '8px', opacity: 0.7 }}>
                    <FileCheck size={20} />
                  </div>
                  <div style={{ opacity: 0.6 }}>
                    <h4 style={{ margin: 0, fontSize: '1rem' }}>IELTS Certification</h4>
                    <p style={{ margin: 0, fontSize: '0.85rem' }}>Optional, but recommended for English programs</p>
                  </div>
                </div>
                <button className="btn btn-outline" style={{ padding: '0.5rem 1rem' }}>Add</button>
              </div>
            </div>
          </section>

          {/* Recent Documents */}
          <section className="glass-card">
            <h3>Document Status</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1.5rem' }}>
              
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <CheckCircle2 size={18} color="var(--text-secondary)" />
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: '0.9rem', margin: 0, color: '#fff' }}>Passport.pdf</p>
                  <p style={{ fontSize: '0.75rem', opacity: 0.6, margin: 0 }}>Verified by AI</p>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Clock size={18} color="#f6ad55" />
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: '0.9rem', margin: 0, color: '#fff' }}>Semester_Transcripts.pdf</p>
                  <p style={{ fontSize: '0.75rem', opacity: 0.6, margin: 0 }}>Processing Data Extraction...</p>
                </div>
              </div>

            </div>
            <button className="btn btn-outline" style={{ width: '100%', marginTop: '1.5rem' }}>
              View All Documents
            </button>
          </section>
        </div>
      </main>
    </div>
  );
}

export default App;
