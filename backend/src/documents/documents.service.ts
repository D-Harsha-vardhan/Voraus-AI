import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { AiService } from '../ai/ai.service';

@Injectable()
export class DocumentsService {
  constructor(
    private prisma: PrismaService,
    private aiService: AiService
  ) {}

  async getDocuments(applicantId: string) {
    return this.prisma.document.findMany({
      where: { applicantId }
    });
  }

  async getDocument(id: string) {
    const doc = await this.prisma.document.findUnique({ where: { id } });
    if (!doc) throw new NotFoundException('Document not found');
    return doc;
  }

  async uploadDocument(applicantId: string, documentType: string, fileData: any) {
    // 1. In real scenario, upload file to Supabase Storage
    const storagePath = `documents/${applicantId}/${Date.now()}_${documentType}.pdf`;

    // 2. Create database record
    const document = await this.prisma.document.create({
      data: {
        applicantId,
        documentType,
        fileName: `${documentType}.pdf`,
        storagePath,
        uploadStatus: 'uploaded',
        processingStatus: 'pending'
      }
    });

    // 3. Trigger processing (in production this would be background queue)
    this.processDocument(document.id).catch(console.error);

    return document;
  }

  async processDocument(id: string) {
    const document = await this.prisma.document.update({
      where: { id },
      data: { processingStatus: 'processing' }
    });

    // Delegate extraction to AI Service (Document Agent)
    const extractionResult = await this.aiService.processDocument(document);

    // Update document with extracted data
    return this.prisma.document.update({
      where: { id },
      data: {
        processingStatus: 'completed',
        extractedData: extractionResult as any,
        verificationStatus: 'verified' // Assume AI can verify for now
      }
    });
  }

  async deleteDocument(id: string) {
    // Should also delete from Supabase storage
    return this.prisma.document.delete({ where: { id } });
  }
}
