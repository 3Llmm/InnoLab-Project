"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Edit, Trash2, Play, Download } from "lucide-react"
import type { Challenge } from "@/lib/types"
import EditChallengeModal from "./edit-challenge-modal"
import { deleteChallenge } from "@/lib/api/challenges"
import { useToast } from "@/hooks/use-toast"

interface ChallengeTableProps {
  challenges: Challenge[]
}

export default function ChallengeTable({ challenges }: ChallengeTableProps) {
  const [editingChallenge, setEditingChallenge] = useState<Challenge | null>(null)
  const [isDeleteLoading, setIsDeleteLoading] = useState<string | null>(null)
  const { toast } = useToast()

  const handleDelete = async (id: string) => {
    if (!confirm("Are you sure you want to delete this challenge?")) {
      return
    }

    setIsDeleteLoading(id)
    try {
      await deleteChallenge(id)
      toast({
        title: "Challenge deleted",
        description: "The challenge has been successfully deleted.",
      })
      // Refresh the page to show updated list
      window.location.reload()
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to delete challenge",
        variant: "destructive",
      })
    } finally {
      setIsDeleteLoading(null)
    }
  }

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case "easy":
        return "bg-green-100 text-green-800"
      case "medium":
        return "bg-yellow-100 text-yellow-800"
      case "hard":
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  const getCategoryColor = (category: string) => {
    switch (category) {
      case "web-exploitation":
        return "bg-blue-100 text-blue-800"
      case "cryptography":
        return "bg-purple-100 text-purple-800"
      case "reverse-engineering":
        return "bg-orange-100 text-orange-800"
      case "binary-exploitation":
        return "bg-red-100 text-red-800"
      case "forensics":
        return "bg-green-100 text-green-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  return (
    <>
      <div className="bg-card border border-border rounded-lg">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Title</TableHead>
              <TableHead>Category</TableHead>
              <TableHead>Difficulty</TableHead>
              <TableHead>Points</TableHead>
              <TableHead>Type</TableHead>
              <TableHead>Docker Image</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {challenges.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center text-muted-foreground py-8">
                  No challenges found. Create your first challenge above.
                </TableCell>
              </TableRow>
            ) : (
              challenges.map((challenge) => (
                <TableRow key={challenge.id}>
                  <TableCell className="font-medium">
                    <div>
                      <div>{challenge.title}</div>
                      <div className="text-sm text-muted-foreground truncate max-w-[200px]">
                        {challenge.description}
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="secondary" className={getCategoryColor(challenge.category)}>
                      {challenge.category.replace("-", " ")}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="secondary" className={getDifficultyColor(challenge.difficulty)}>
                      {challenge.difficulty}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className="font-mono">
                      {challenge.points}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className="flex items-center gap-1 w-20 justify-center">
                      {challenge.requiresInstance ? (
                        <>
                          <Play className="h-3 w-3" />
                          Instance
                        </>
                      ) : (
                        <>
                          <Download className="h-3 w-3" />
                          Static
                        </>
                      )}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm text-muted-foreground font-mono max-w-[150px] truncate">
                      {challenge.dockerImageName || "N/A"}
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setEditingChallenge(challenge)}
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => handleDelete(challenge.id)}
                        disabled={isDeleteLoading === challenge.id}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <EditChallengeModal
        challenge={editingChallenge}
        isOpen={!!editingChallenge}
        onClose={() => setEditingChallenge(null)}
        onSave={() => {
          setEditingChallenge(null)
          window.location.reload() // Refresh to show updated data
        }}
      />
    </>
  )
}